package com.quatro.settlement_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
// Ativar um profile de teste ajuda a não tentar conectar no Kafka real caso rode o teste localmente
@ActiveProfiles("test") 
class SettlementServiceApplicationTests {

	@Test
	void contextLoads() {
        // Se a aplicação rodar este método e passar com sucesso (verde),
        // significa que as anotações, conexões de banco e gRPC estão configuradas corretamente.
	}

}