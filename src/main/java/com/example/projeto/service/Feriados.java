package com.example.projeto.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feriados nacionais do Brasil, municipais de Blumenau e folgas fixas da empresa,
 * usados para descontar dias não úteis do leadtime.
 *
 * Os feriados móveis (Carnaval, Sexta-feira Santa e Corpus Christi) são calculados a
 * partir da Páscoa de cada ano, então a lista não precisa ser atualizada todo ano.
 * Carnaval e Corpus Christi são ponto facultativo pela lei, mas entram aqui porque a
 * empresa não trabalha nesses dias.
 */
public final class Feriados {

    private static final Set<MonthDay> FIXOS = Set.of(
            MonthDay.of(1, 1),    // Confraternização Universal
            MonthDay.of(4, 21),   // Tiradentes
            MonthDay.of(5, 1),    // Dia do Trabalho
            MonthDay.of(9, 2),    // Aniversário de Blumenau (feriado municipal)
            MonthDay.of(9, 7),    // Independência
            MonthDay.of(10, 12),  // Nossa Senhora Aparecida
            MonthDay.of(11, 2),   // Finados
            MonthDay.of(11, 15),  // Proclamação da República
            MonthDay.of(11, 20),  // Dia Nacional de Zumbi e da Consciência Negra
            MonthDay.of(12, 24),  // Véspera de Natal (folga da empresa)
            MonthDay.of(12, 25)   // Natal
    );

    private static final Map<Integer, Set<LocalDate>> MOVEIS_POR_ANO = new ConcurrentHashMap<>();

    private Feriados() {
    }

    public static boolean isFeriado(LocalDate data) {
        return FIXOS.contains(MonthDay.from(data))
                || MOVEIS_POR_ANO.computeIfAbsent(data.getYear(), Feriados::moveis).contains(data);
    }

    /** Dia útil = segunda a sexta que não seja feriado. */
    public static boolean isDiaUtil(LocalDate data) {
        DayOfWeek dow = data.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY && !isFeriado(data);
    }

    private static Set<LocalDate> moveis(int ano) {
        LocalDate pascoa = pascoa(ano);
        Set<LocalDate> datas = new HashSet<>();
        datas.add(pascoa.minusDays(48)); // Segunda de Carnaval
        datas.add(pascoa.minusDays(47)); // Terça de Carnaval
        datas.add(pascoa.minusDays(2));  // Sexta-feira Santa
        datas.add(pascoa.plusDays(60));  // Corpus Christi
        return datas;
    }

    /** Domingo de Páscoa pelo algoritmo de Meeus/Jones/Butcher (calendário gregoriano). */
    static LocalDate pascoa(int ano) {
        int a = ano % 19;
        int b = ano / 100;
        int c = ano % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(ano, mes, dia);
    }
}
