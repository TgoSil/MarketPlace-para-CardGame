package com.quatro.profile_service.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.quatro.profile_service.domain.dto.CarteiraRequestDto;
import com.quatro.profile_service.domain.dto.CarteiraResponseDto;
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
    public CarteiraResponseDto criarCarteira(UUID userId){
        Optional<Carteira> carteiraExistente = repository.findById(userId);

        Carteira carteira;
        if(!carteiraExistente.isPresent()){
            carteira = Carteira.builder()
                    .userId(userId)
                    .dinheiro(0)
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

    private CarteiraResponseDto converterParaDto(Carteira carteira) {
        return CarteiraResponseDto.builder()
                .dinheiro((double)carteira.getDinheiro()/100)
                .build();
    }
}
