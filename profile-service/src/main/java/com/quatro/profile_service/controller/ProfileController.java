package com.quatro.profile_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.quatro.profile_service.domain.dto.CarteiraRequestDto;
import com.quatro.profile_service.domain.dto.CarteiraResponseDto;
import com.quatro.profile_service.domain.dto.UsuarioResponseDto;
import com.quatro.profile_service.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping("/usuario")
    public ResponseEntity<CarteiraResponseDto> adicionarNaCarteira(
            @RequestHeader("User-Id") UUID userId,
            @RequestHeader("User-cargo") String cargo,
            @RequestBody CarteiraRequestDto requestDto){

        if(!cargo.equals("ADMIN")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(profileService.adicionarNaCarteira(userId, requestDto));
    }

    @PostMapping("/usuario/{userId}")
    public ResponseEntity<CarteiraResponseDto> adicionarNaCarteiraDoUsuario(
            @PathVariable UUID userId,
            @RequestHeader("User-cargo") String cargo,
            @RequestBody CarteiraRequestDto requestDto){

        if(!cargo.equals("ADMIN")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(profileService.adicionarNaCarteira(userId, requestDto));
    }

    @GetMapping("/usuario")
    public ResponseEntity<CarteiraResponseDto> buscarCarteira(
        @RequestHeader("User-Id") UUID userId,
        @RequestHeader("User-cargo") String cargo){
        CarteiraResponseDto perfil = profileService.buscarCarteira(userId);
        perfil.setCargo(cargo);
        return ResponseEntity.ok(perfil);
    }

    @GetMapping("/admin/usuarios")
    public ResponseEntity<List<UsuarioResponseDto>> getAllUsuarios(
            @RequestHeader(value = "User-cargo", defaultValue = "") String userCargo) {
        
        if (!"ADMIN".equalsIgnoreCase(userCargo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<UsuarioResponseDto> usuarios = profileService.listarTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @DeleteMapping("/usuario")
    public ResponseEntity<CarteiraResponseDto> removerDaCarteira(
            @RequestHeader("User-Id") UUID userId,
            @RequestHeader("User-cargo") String cargo,
            @RequestBody CarteiraRequestDto requestDto){

        if(!cargo.equals("ADMIN")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(profileService.removerDaCarteira(userId, requestDto));
    }

    @DeleteMapping("/usuario/{userId}")
    public ResponseEntity<CarteiraResponseDto> deletarUsuario(
            @PathVariable UUID userId,
            @RequestHeader("User-cargo") String cargo){

        if(!cargo.equals("ADMIN")) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if(profileService.removerUsuario(userId))return ResponseEntity.noContent().build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
