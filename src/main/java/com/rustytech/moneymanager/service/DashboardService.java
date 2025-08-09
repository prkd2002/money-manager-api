package com.rustytech.moneymanager.service;

import com.rustytech.moneymanager.dtos.ExpenseDto;
import com.rustytech.moneymanager.dtos.IncomeDto;
import com.rustytech.moneymanager.dtos.RecentTransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final IncomeService incomeService;
    private final ProfileService profileService;
    private final ExpenseService expenseService;

    public Map<String,Object> getDashBoardData(){
        var profile = profileService.getCurrentProfile();
        Map<String, Object> returnValue = new LinkedHashMap<>();
        List<IncomeDto> latestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDto> latestExpenses = expenseService.getLatest5ExpensesForCurrentUser();
        List<RecentTransactionDto> recentTransactionDtos = concat(latestIncomes.stream().map(income -> RecentTransactionDto.builder()
                .id(income.getId())
                .name(income.getName())
                .icon(income.getIcon())
                .date(income.getDate())
                .profileId(profile.getId())
                .amount(income.getAmount())
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .type("Income")
                .build()), latestExpenses.stream().map(expense -> RecentTransactionDto.builder()
                .id(expense.getId())
                .name(expense.getName())
                .icon(expense.getIcon())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .profileId(profile.getId())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .type("Expense").build())).sorted((a,b) ->{
                    int cmp = b.getDate().compareTo(a.getDate());
                    if(cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null){
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return cmp;
        } ).toList();

        returnValue.put("totalBalance", incomeService.getTotalIncomeForCurrentUser().subtract(expenseService.getTotalExpenseForCurrentUser()));
        returnValue.put("totalIncome", incomeService.getTotalIncomeForCurrentUser());
        returnValue.put("totalExpense", expenseService.getTotalExpenseForCurrentUser());
        returnValue.put("recent5Expenses", latestExpenses);
        returnValue.put("recent5Incomes", latestIncomes);
        returnValue.put("recentTransactions", recentTransactionDtos);
        return returnValue;

    }

}
