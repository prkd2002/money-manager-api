package com.rustytech.moneymanager.service;

import com.rustytech.moneymanager.dtos.ExpenseDto;
import com.rustytech.moneymanager.dtos.IncomeDto;
import com.rustytech.moneymanager.dtos.ProfileDto;
import com.rustytech.moneymanager.entity.CategoryEntity;
import com.rustytech.moneymanager.entity.ExpenseEntity;
import com.rustytech.moneymanager.entity.IncomeEntity;
import com.rustytech.moneymanager.entity.Profile;
import com.rustytech.moneymanager.exceptions.CategoryNotFoundException;
import com.rustytech.moneymanager.exceptions.ExpenseNotFoundException;
import com.rustytech.moneymanager.exceptions.UnauthorizeException;
import com.rustytech.moneymanager.repository.CategoryRepository;
import com.rustytech.moneymanager.repository.ExpenseRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ProfileService profileService;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseDto addExpense(ExpenseDto expenseDto){
        Profile profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(expenseDto.getCategoryId()).orElseThrow(()->new CategoryNotFoundException("Category not found"));
        var newExpense = toEntity(expenseDto, profile, category);
        newExpense = expenseRepository.save(newExpense);
        return toDto(newExpense);
    }

    // Retrieve all expenses for current month/based on the start date and end date
    public List<ExpenseDto> getCurrentMonthExpensesForCurrentUser(){
        Profile profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        var expenseList = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return expenseList.stream().map(this::toDto).collect(Collectors.toList());

    }


    // Delete Expense by id for current User
    public void deleteExpense(Long expenseId){
        var profile = profileService.getCurrentProfile();
        var expense = expenseRepository.findById(expenseId).orElseThrow(() -> new ExpenseNotFoundException("Expense with Id " + expenseId + " was not found !"));
        if(!expense.getProfile().getId().equals(profile.getId())){
            throw new UnauthorizeException("Unauthorized to delete this expense !");
        }
        expenseRepository.delete(expense);

    }

    // Get latest 5 Expenses for current User
    public List<ExpenseDto>  getLatest5ExpensesForCurrentUser(){
        Profile profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenseList = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return expenseList.stream().map(this::toDto).collect(Collectors.toList());
    }


    // Get Total Expenses for current User
    public BigDecimal getTotalExpenseForCurrentUser(){
        var profile = profileService.getCurrentProfile();
        BigDecimal totalExpense = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return totalExpense == null ? BigDecimal.ZERO : totalExpense;
    }


    // Filter Expense
    public List<ExpenseDto> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort){
        var profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenseList = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return expenseList.stream().map(this::toDto).collect(Collectors.toList());

    }

    // Notifications
    public List<ExpenseDto> getExpensesForUserOnDate(Long profileId, LocalDate date){
        List<ExpenseEntity> expenseEntityList = expenseRepository.findByProfileIdAndDate(profileId, date);
        return expenseEntityList.stream().map(this::toDto).collect(Collectors.toList());
    }
    private ExpenseEntity toEntity(ExpenseDto expenseDto, Profile profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(expenseDto.getName())
                .icon(expenseDto.getIcon())
                .amount(expenseDto.getAmount())
                .category(category)
                .profile(profile)
                .date(expenseDto.getDate())
                .build();
    }

    private ExpenseDto toDto(ExpenseEntity expenseEntity) {
        return ExpenseDto.builder()
                .id(expenseEntity.getId())
                .name(expenseEntity.getName())
                .icon(expenseEntity.getIcon())
                .categoryId(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getId() : null )
                .categoryName(expenseEntity.getCategory() != null ?  expenseEntity.getCategory().getName(): null)
                .amount(expenseEntity.getAmount())
                .date(expenseEntity.getDate()).createdAt(expenseEntity.getCreatedAt()).updatedAt(expenseEntity.getUpdatedAt()).
                build();
    }

    public ByteArrayInputStream expensesToExcel(List<ExpenseDto> incomes){
        String[] HEADERS = {"S.No","Name","Category","Amount","Date"};
        try(Workbook wb = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream()){
            Sheet sheet = wb.createSheet("Expenses");

            // Header
            Row headerRow = sheet.createRow(0);
            for(int i= 0; i < HEADERS.length; i++){
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
            }

            // Data rows
            int rowIdx = 1;
            int serial = 1;
            for(ExpenseDto expenseDto : incomes){
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(serial++);
                row.createCell(1).setCellValue(expenseDto.getName());
                row.createCell(2).setCellValue(expenseDto.getCategoryName());
                row.createCell(3).setCellValue(expenseDto.getAmount().toString());
                row.createCell(4).setCellValue(expenseDto.getDate().toString());
            }
            wb.write(out);
            return new ByteArrayInputStream(out.toByteArray());


        }catch (Exception e){
            throw new RuntimeException("Failed to export data to Excel file: "+ e.getMessage());

        }
    }
}
