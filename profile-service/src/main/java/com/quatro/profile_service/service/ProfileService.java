package com.quatro.profile_service.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.quatro.profile_service.domain.dto.CarteiraRequestDto;
import com.quatro.profile_service.domain.dto.CarteiraResponseDto;
import com.quatro.profile_service.domain.dto.UsuarioResponseDto;
import com.quatro.profile_service.domain.entity.Carteira;
import com.quatro.profile_service.repository.CarteiraRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final CarteiraRepository repository;

    // -- UPDATE --
    @Transactional
    public CarteiraResponseDto adicionarNaCarteira(UUID userId, CarteiraRequestDto request){
        int quantidadeParaAdicionar = request.getDinheiro() != null ? request.getDinheiro() : 0;

        Optional<Carteira> carteiraExistente = repository.findById(userId);

        Carteira carteira;
        if(carteiraExistente.isPresent()){
            carteira = carteiraExistente.get();
            carteira.setDinheiro(carteira.getDinheiro() + quantidadeParaAdicionar);    
        }else {
            throw new RuntimeException("Usuário não encontrado.");
        }

        Carteira salvo = repository.save(carteira);
        return converterParaDto(salvo);
    }

    // -- CREATE --
    @Transactional
    public CarteiraResponseDto criarCarteira(UUID userId, String username){
        Optional<Carteira> carteiraExistente = repository.findById(userId);

        Carteira carteira;
        if(!carteiraExistente.isPresent()){
            carteira = Carteira.builder()
                    .userId(userId)
                    .dinheiro(0)
                    .username(username)
                    .criadoEm(LocalDate.now())
                    .build();
        }else {
            throw new RuntimeException("Usuário já existe.");
        }

        Carteira salvo = repository.save(carteira);
        return converterParaDto(salvo);
    }

    // -- READ --
    public CarteiraResponseDto buscarCarteira(UUID userId){
        Optional<Carteira> carteiraExistente = repository.findById(userId);

        Carteira carteira;
        if(carteiraExistente.isPresent()){
            carteira = carteiraExistente.get();
        }else{
            throw new RuntimeException("Usuário não encontrado.");
        }

        return converterParaDto(carteira);
    }

    public List<UsuarioResponseDto> listarTodosUsuarios() {
        return repository.findAll().stream()
            .map(carteira -> UsuarioResponseDto.builder()
                .id(carteira.getUserId())
                .username(carteira.getUsername())
                .dinheiro((double)carteira.getDinheiro()/100)
                .criadoEm(carteira.getCriadoEm().atStartOfDay(ZoneId.systemDefault()).toInstant())
                .build())
            .collect(Collectors.toList());
    }

    // -- DELETE --
    @Transactional
    public boolean removerUsuario(UUID userId){
        Optional<Carteira> carteiraExistente = repository.findById(userId);
        if(carteiraExistente.isPresent()){
            repository.deleteById(userId);
            return true;
        }
        return false;
    }

    @Transactional
    public CarteiraResponseDto removerDaCarteira(UUID userId, CarteiraRequestDto request){
        int quantidadeParaRemover = request.getDinheiro() != null ? request.getDinheiro() : 0;

        Optional<Carteira> carteiraExistente = repository.findById(userId);

        Carteira carteira;
        if(carteiraExistente.isPresent()){
            carteira = carteiraExistente.get();
            if(carteira.getDinheiro()>=quantidadeParaRemover){
                carteira.setDinheiro(carteira.getDinheiro() - quantidadeParaRemover);  
            }else{
                throw new RuntimeException("Dinheiro insuficiente.");
            }
        }else {
            throw new RuntimeException("Usuário não encontrado.");
        }

        Carteira salvo = repository.save(carteira);
        return converterParaDto(salvo);
    }

    // -- TRANSFERÊNCIA (gRPC) --
    @Transactional
    public void processarTransferencia(UUID compradorId, UUID vendedorId, double precoCarta) {
        // Converte o double recebido pelo gRPC para o Integer (centavos) usado no banco de dados
        int valorTransferencia = (int) Math.round(precoCarta * 100);

        if (valorTransferencia <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero.");
        }

        // 1. Valida e debita do Comprador
        Carteira carteiraComprador = repository.findById(compradorId)
                .orElseThrow(() -> new RuntimeException("Carteira do comprador não encontrada."));

        if (carteiraComprador.getDinheiro() < valorTransferencia) {
            throw new RuntimeException("Saldo insuficiente para realizar a compra.");
        }
        
        carteiraComprador.setDinheiro(carteiraComprador.getDinheiro() - valorTransferencia);
        repository.save(carteiraComprador);

        // 2. Valida e credita no Vendedor
        Carteira carteiraVendedor = repository.findById(vendedorId)
                .orElseThrow(() -> new RuntimeException("Carteira do vendedor não encontrada."));

        carteiraVendedor.setDinheiro(carteiraVendedor.getDinheiro() + valorTransferencia);
        repository.save(carteiraVendedor);
    }

    private CarteiraResponseDto converterParaDto(Carteira carteira) {
        return CarteiraResponseDto.builder()
                .dinheiro((double)carteira.getDinheiro()/100)
                .username(carteira.getUsername())
                .criadoEm(carteira.getCriadoEm())
                .build();
    }
}
