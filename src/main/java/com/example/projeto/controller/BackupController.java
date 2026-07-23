package com.example.projeto.controller;

import com.example.projeto.service.CsvBackupService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/** Dispara um backup CSV sob demanda (além do backup automático periódico). */
@Controller
@RequestMapping("/backup")
public class BackupController {

    private final CsvBackupService backupService;

    public BackupController(CsvBackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping
    @ResponseBody
    public String backupAgora() {
        int total = backupService.backupTodos();
        return "Backup CSV gerado com sucesso (" + total + " registros) em: " + backupService.getBackupDir();
    }
}
