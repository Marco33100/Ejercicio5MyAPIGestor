package com.mx.cbtis.gestor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

final class SolicitudesTest {
    private SolicitudesTest() { }
    static <T> T convertir(Object entrada, Class<T> tipo) {
        return new ObjectMapper().convertValue(entrada, tipo);
    }
}
