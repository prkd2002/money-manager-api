package com.rustytech.moneymanager.controller;

import com.rustytech.moneymanager.dtos.ExpenseDto;
import com.rustytech.moneymanager.dtos.IncomeDto;
import com.rustytech.moneymanager.exceptions.CategoryNotFoundException;
import com.rustytech.moneymanager.exceptions.ExpenseNotFoundException;
import com.rustytech.moneymanager.exceptions.UnauthorizeException;
import com.rustytech.moneymanager.service.EmailService;
import com.rustytech.moneymanager.service.ExpenseService;
import com.rustytech.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;
    private final ProfileService profileService;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<ExpenseDto> saveExpense(@RequestBody ExpenseDto expenseDto) {
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.addExpense(expenseDto)    );
        }catch (CategoryNotFoundException e){
            throw new CategoryNotFoundException(e.getMessage());
        }
    }


    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getExpensesForCurrentUser() {
        return ResponseEntity.ok(expenseService.getCurrentMonthExpensesForCurrentUser());
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long expenseId) {
        try{
            expenseService.deleteExpense(expenseId);
            return ResponseEntity.noContent().build();
        }catch (UnauthorizeException e){
            throw new UnauthorizeException(e.getMessage());
        }catch (ExpenseNotFoundException e){
            throw new ExpenseNotFoundException(e.getMessage());
        }


    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadIncome() {
        var expenses = expenseService.getCurrentMonthExpensesForCurrentUser();
        ByteArrayInputStream in = expenseService.expensesToExcel(expenses);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment;filename=income.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(in.readAllBytes());
    }



    @GetMapping("/email")
    public ResponseEntity<?> sendIncomesExcelByEmail(){
        var profile = profileService.getCurrentProfile();
        var incomes = expenseService.getCurrentMonthExpensesForCurrentUser();
        ByteArrayInputStream in = expenseService.expensesToExcel(incomes);
        try{
            byte[] excelData = in.readAllBytes();
            emailService.senEmailWithAttachment(
                    profile.getEmail(),
                    "Ihr Ausgabensbericht",
                    String.format("""
                <html>
                    <body style="font-family: Arial, sans-serif; color: #333;">
                        <h2>Guten Tag, %s</h2>
                        <p>anbei erhalten Sie Ihren aktuellen Ausgabensbericht als Excel-Datei.</p>
                        <p>Wir danken Ihnen für die Nutzung unseres Services und wünschen Ihnen weiterhin viel Erfolg bei der Verwaltung Ihrer Finanzen.</p>
                        <br/>
                        <p>Mit freundlichen Grüßen,<br/>
                        Ihr MoneyManager-Team</p>
                    </body>
                </html>
                """, profile.getFullName()),
                    excelData,
                    "Ausgabensbericht.xlsx"
            );

            return ResponseEntity.ok("Email sent Successfully");

        }catch (Exception e) {
            throw new RuntimeException("Failed to send email with attachment",e);
        }
    }




}
