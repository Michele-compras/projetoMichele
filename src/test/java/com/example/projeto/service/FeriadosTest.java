package com.example.projeto.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeriadosTest {

    @Test
    void pascoaDeAnosConhecidos() {
        assertEquals(LocalDate.of(2025, 4, 20), Feriados.pascoa(2025));
        assertEquals(LocalDate.of(2026, 4, 5), Feriados.pascoa(2026));
        assertEquals(LocalDate.of(2027, 3, 28), Feriados.pascoa(2027));
    }

    @Test
    void feriadosMoveisDe2026() {
        assertTrue(Feriados.isFeriado(LocalDate.of(2026, 2, 16)));  // Segunda de Carnaval
        assertTrue(Feriados.isFeriado(LocalDate.of(2026, 2, 17)));  // Terça de Carnaval
        assertTrue(Feriados.isFeriado(LocalDate.of(2026, 4, 3)));   // Sexta-feira Santa
        assertTrue(Feriados.isFeriado(LocalDate.of(2026, 6, 4)));   // Corpus Christi
        assertFalse(Feriados.isFeriado(LocalDate.of(2026, 2, 18))); // Quarta de Cinzas
    }

    @Test
    void diaUtil() {
        assertTrue(Feriados.isDiaUtil(LocalDate.of(2026, 10, 8)));   // quinta comum
        assertFalse(Feriados.isDiaUtil(LocalDate.of(2026, 10, 10))); // sábado
        assertFalse(Feriados.isDiaUtil(LocalDate.of(2026, 10, 11))); // domingo
        assertFalse(Feriados.isDiaUtil(LocalDate.of(2026, 10, 12))); // N. Sra. Aparecida (segunda)
        assertFalse(Feriados.isDiaUtil(LocalDate.of(2026, 11, 20))); // Consciência Negra (sexta)
        assertFalse(Feriados.isDiaUtil(LocalDate.of(2026, 9, 2)));   // Aniversário de Blumenau (quarta)
        assertFalse(Feriados.isDiaUtil(LocalDate.of(2026, 12, 24))); // Véspera de Natal (quinta)
        assertTrue(Feriados.isDiaUtil(LocalDate.of(2026, 12, 31)));  // 31/12 é dia útil na empresa (quinta)
        assertFalse(Feriados.isDiaUtil(LocalDate.of(2027, 1, 1)));   // Confraternização Universal (sexta)
    }
}
