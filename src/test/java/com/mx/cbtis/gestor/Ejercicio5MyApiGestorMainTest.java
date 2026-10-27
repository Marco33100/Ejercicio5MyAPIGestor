package com.mx.cbtis.gestor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import com.mx.cbtis.gestor.controller.UsuarioController;

public class Ejercicio5MyApiGestorMainTest {
    public static class CapturarContexto implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        static ConfigurableApplicationContext contexto;

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            contexto = applicationContext;
        }
    }

    @Test
    void mainArrancaLaApiConSusControladoresYSePuedeCerrar() {
        try {
            Ejercicio5MyApiGestorApplication.main(new String[] {
                    "--spring.main.web-application-type=none",
                    "--context.initializer.classes=" + CapturarContexto.class.getName()
            });
            assertNotNull(CapturarContexto.contexto);
            assertTrue(CapturarContexto.contexto.isActive());
            assertNotNull(CapturarContexto.contexto.getBean(UsuarioController.class));
        } finally {
            if (CapturarContexto.contexto != null) {
                CapturarContexto.contexto.close();
                CapturarContexto.contexto = null;
            }
        }
    }
}
