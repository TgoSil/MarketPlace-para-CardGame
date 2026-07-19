package com.quatro.catalog_service.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quatro.catalog_service.domain.dto.CartaEvento;
import com.quatro.catalog_service.domain.dto.CartaRequestDto;
import com.quatro.catalog_service.domain.dto.CartaResponseDto;
import com.quatro.catalog_service.domain.entity.cartas;
import com.quatro.catalog_service.repository.CatalogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogRepository catalogRepository;
    
    // Injeta a ferramenta oficial do Spring para enviar mensagens pro Kafka
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Puxa o nome do tópico lá do seu application.properties
    @Value("${topico.catalogo.carta-evento}")
    private String topicoCartaEvento;

    // 1. Criar ou Atualizar uma carta
    public CartaResponseDto salvarOuAtualizarCarta(UUID id, CartaRequestDto requestDto) {
        cartas cartaParaSalvar;
        String acao; // Guardamos a ação para avisar o Kafka depois

        if (id != null) {
            acao = "ATUALIZADA";
            cartaParaSalvar = catalogRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Carta não encontrada para edição."));
            
            cartaParaSalvar.setNome(requestDto.getNome());
            cartaParaSalvar.setTipo(requestDto.getTipo());
            cartaParaSalvar.setRaridade(requestDto.getRaridade());
            cartaParaSalvar.setVida(requestDto.getVida());
            cartaParaSalvar.setDescricao(requestDto.getDescricao());
            cartaParaSalvar.setImagemUrl(requestDto.getImagemUrl());
        } else {
            acao = "CRIADA";
            cartaParaSalvar = cartas.builder()
                    .nome(requestDto.getNome())
                    .tipo(requestDto.getTipo())
                    .raridade(requestDto.getRaridade())
                    .vida(requestDto.getVida())
                    .descricao(requestDto.getDescricao())
                    .imagemUrl(requestDto.getImagemUrl())
                    .build();
        }

        cartas cartaSalva = catalogRepository.save(cartaParaSalvar);
        CartaResponseDto responseDto = converterParaResponseDto(cartaSalva);

        // DISPARO PRO KAFKA: Avisa todo o sistema que a carta mudou!
        enviarEventoKafka(acao, responseDto, responseDto.getId());

        return responseDto;
    }

    // 2. Exibir e Filtrar cartas (Permanece igual)
    public List<CartaResponseDto> filtrarCartas(String nome, String raridade, String tipo) {
        List<cartas> resultado;

        if (nome != null && !nome.isBlank()) {
            resultado = catalogRepository.findByNome(nome);
        } else if (raridade != null && !raridade.isBlank()) {
            resultado = catalogRepository.findByRaridade(raridade);
        } else if (tipo != null && !tipo.isBlank()) {
            resultado = catalogRepository.findByTipo(tipo);
        } else {
            resultado = catalogRepository.findAll();
        }

        return resultado.stream()
                .map(this::converterParaResponseDto)
                .collect(Collectors.toList());
    }

    // 3. Remover uma carta
    @Transactional
    public void removerCarta(UUID cartaId) {
        catalogRepository.deleteById(cartaId);
        
        // DISPARO PRO KAFKA: Avisa que a carta foi excluída
        enviarEventoKafka("DELETADA", null, cartaId);
    }

    // Método auxiliar de conversão (Permanece igual)
    private CartaResponseDto converterParaResponseDto(cartas carta) {
        return CartaResponseDto.builder()
                .id(carta.getCartaId())
                .nome(carta.getNome())
                .tipo(carta.getTipo())
                .raridade(carta.getRaridade())
                .vida(carta.getVida())
                .descricao(carta.getDescricao())
                .imagemUrl(carta.getImagemUrl())
                .criadoEm(carta.getCriadoEm())
                .atualizadoEm(carta.getAtualizadoEm())
                .build();
    }

    // --- LÓGICA DO KAFKA ---
    private void enviarEventoKafka(String acao, CartaResponseDto carta, UUID cartaId) {
        String nome = carta != null ? carta.getNome() : null;
        String raridade = carta != null ? carta.getRaridade() : null;
        CartaEvento evento = new CartaEvento(cartaId, nome, raridade, acao);
        // Envia para o tópico. O primeiro parâmetro é o tópico, o segundo é a chave (ID), o terceiro é a mensagem
        kafkaTemplate.send(topicoCartaEvento, cartaId.toString(), evento);
        
        System.out.println("Enviado para o Kafka -> Ação: " + acao + " | ID da Carta: " + cartaId);
    }
}