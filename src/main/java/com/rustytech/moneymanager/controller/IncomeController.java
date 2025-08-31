package com.rustytech.moneymanager.controller;

import com.rustytech.moneymanager.dtos.ExpenseDto;
import com.rustytech.moneymanager.dtos.IncomeDto;
import com.rustytech.moneymanager.entity.IncomeEntity;
import com.rustytech.moneymanager.exceptions.CategoryNotFoundException;
import com.rustytech.moneymanager.exceptions.ExpenseNotFoundException;
import com.rustytech.moneymanager.exceptions.UnauthorizeException;
import com.rustytech.moneymanager.service.EmailService;
import com.rustytech.moneymanager.service.IncomeService;
import com.rustytech.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeController {
    private final IncomeService incomeService;
    private final ProfileService profileService;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<IncomeDto> saveIncome(@RequestBody IncomeDto incomeDto) {
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(incomeService.addIncome(incomeDto)    );

        }catch (CategoryNotFoundException e){
            throw new CategoryNotFoundException(e.getMessage());
        }
    }



    @GetMapping
    public ResponseEntity<List<IncomeDto>> getExpensesForCurrentUser() {
        return ResponseEntity.ok(incomeService.getCurrentMonthIncomesForCurrentUser());
    }


    @DeleteMapping("/{incomeId}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long incomeId) {
        try{
            incomeService.deleteIncome(incomeId);
            return ResponseEntity.noContent().build();
        }catch (UnauthorizeException e){
            throw new UnauthorizeException(e.getMessage());
        }catch (ExpenseNotFoundException e){
            throw new ExpenseNotFoundException(e.getMessage());
        }

    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadIncome() {
        List<IncomeDto> incomes = incomeService.getCurrentMonthIncomesForCurrentUser();
        ByteArrayInputStream in = incomeService.incomesToExcel(incomes);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment;filename=income.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(in.readAllBytes());
    }



    @GetMapping("/email")
    public ResponseEntity<?> sendIncomesExcelByEmail(){
        var profile = profileService.getCurrentProfile();
        var incomes = incomeService.getCurrentMonthIncomesForCurrentUser();
        ByteArrayInputStream in = incomeService.incomesToExcel(incomes);
        try{
            byte[] excelData = in.readAllBytes();
            emailService.senEmailWithAttachment(
                    profile.getEmail(),
                    "Ihr Einkommensbericht",
                    String.format("""
                <html>
                    <body style="font-family: Arial, sans-serif; color: #333;">
                        <h2>Guten Tag, %s</h2>
                        <p>anbei erhalten Sie Ihren aktuellen Einkommensbericht als Excel-Datei.</p>
                        <p>Wir danken Ihnen für die Nutzung unseres Services und wünschen Ihnen weiterhin viel Erfolg bei der Verwaltung Ihrer Finanzen.</p>
                        <br/>
                        <p>Mit freundlichen Grüßen,<br/>
                        Ihr MoneyManager-Team</p>
                    </body>
                </html>
                """, profile.getFullName()),
                    excelData,
                    "Einkommensbericht.xlsx"
            );

            return ResponseEntity.ok("Email sent Successfully");

        }catch (Exception e) {
            throw new RuntimeException("Failed to send email with attachment",e);
        }
    }








}
