package com.example.projeto.service;

import com.example.projeto.model.FichaTecnica;
import com.example.projeto.model.TipoItem;
import com.example.projeto.repository.FichaTecnicaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportacaoService {

    private final FichaTecnicaRepository repository;

    public ImportacaoService(FichaTecnicaRepository repository) {
        this.repository = repository;
    }

    public int importarExcel(MultipartFile arquivo) throws Exception {
        List<FichaTecnica> fichas = new ArrayList<>();

        try (InputStream is = arquivo.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new RuntimeException("Planilha vazia");
            }

            // Mapear colunas pelo cabeçalho
            int colEstagio = -1, colTipo = -1, colTipoSolicitacao = -1, colCodigo = -1;
            int colFornecedor = -1, colReferencia = -1, colCategoria = -1;
            int colMarcaComprou = -1, colMarcaUtiliza = -1;
            int colDescricao = -1, colCor = -1, colComposicao = -1, colGramatura = -1;
            int colLargura = -1, colNumeroPedido = -1;

            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                String header = getCellString(headerRow.getCell(c)).toUpperCase().trim();
                switch (header) {
                    case "ESTÁGIO", "ESTAGIO" -> colEstagio = c;
                    case "TIPO DE SOLICITAÇÃO", "TIPO DE SOLICITACAO", "TIPO SOLICITAÇÃO", "TIPO SOLICITACAO" -> colTipoSolicitacao = c;
                    case "TIPO" -> colTipo = c;
                    case "CÓDIGO", "CODIGO" -> colCodigo = c;
                    case "FORNECEDOR" -> colFornecedor = c;
                    case "REFERÊNCIA", "REFERENCIA" -> colReferencia = c;
                    case "CATEGORIA" -> colCategoria = c;
                    case "MARCA QUE COMPROU" -> colMarcaComprou = c;
                    case "MARCA QUE UTILIZA" -> colMarcaUtiliza = c;
                    case "DESCRIÇÃO", "DESCRICAO" -> colDescricao = c;
                    case "COR" -> colCor = c;
                    case "COMPOSIÇÃO", "COMPOSICAO" -> colComposicao = c;
                    case "GRAMATURA" -> colGramatura = c;
                    case "LARGURA" -> colLargura = c;
                    case "Nº PEDIDO", "NUMERO PEDIDO", "N PEDIDO", "NR PEDIDO" -> colNumeroPedido = c;
                }
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                FichaTecnica ficha = new FichaTecnica();

                // Tipo
                if (colTipo >= 0) {
                    String tipoStr = getCellString(row.getCell(colTipo)).toUpperCase().trim();
                    if (tipoStr.contains("METRO")) {
                        ficha.setTipo(TipoItem.ACESSORIO_METRO);
                    } else if (tipoStr.contains("ACESS") || tipoStr.contains("UNID") || tipoStr.contains("AVIAM")) {
                        ficha.setTipo(TipoItem.ACESSORIO_UNIDADE);
                    } else {
                        ficha.setTipo(TipoItem.TECIDO);
                    }
                } else {
                    ficha.setTipo(TipoItem.TECIDO);
                }

                // Descrição (obrigatório) - tenta coluna descrição ou referência
                String desc = colDescricao >= 0 ? getCellString(row.getCell(colDescricao)) : "";
                if (desc.isBlank() && colReferencia >= 0) {
                    desc = getCellString(row.getCell(colReferencia));
                }
                if (desc.isBlank()) {
                    desc = "Item importado #" + r;
                }
                ficha.setDescricao(desc);

                // Cor (obrigatório)
                String cor = colCor >= 0 ? getCellString(row.getCell(colCor)) : "";
                ficha.setCor(cor.isBlank() ? "-" : cor);

                // Campos texto
                if (colEstagio >= 0) ficha.setEstagio(getCellString(row.getCell(colEstagio)));
                if (colTipoSolicitacao >= 0) ficha.setTipoSolicitacao(getCellString(row.getCell(colTipoSolicitacao)));
                if (colCodigo >= 0) ficha.setCodigo(getCellString(row.getCell(colCodigo)));
                if (colFornecedor >= 0) ficha.setFornecedor(getCellString(row.getCell(colFornecedor)));
                if (colReferencia >= 0) ficha.setReferencia(getCellString(row.getCell(colReferencia)));
                if (colCategoria >= 0) ficha.setCategoriaProduto(getCellString(row.getCell(colCategoria)));
                if (colMarcaComprou >= 0) ficha.setMarcaQueComprou(getCellString(row.getCell(colMarcaComprou)));
                if (colMarcaUtiliza >= 0) ficha.setMarcaQueUtiliza(getCellString(row.getCell(colMarcaUtiliza)));
                if (colComposicao >= 0) ficha.setComposicao(getCellString(row.getCell(colComposicao)));
                if (colGramatura >= 0) ficha.setGramatura(getCellString(row.getCell(colGramatura)));
                if (colLargura >= 0) ficha.setLargura(getCellString(row.getCell(colLargura)));
                if (colNumeroPedido >= 0) ficha.setNumeroPedido(getCellString(row.getCell(colNumeroPedido)));

                fichas.add(ficha);
            }
        }

        repository.saveAll(fichas);
        return fichas.size();
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !getCellString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }
}
