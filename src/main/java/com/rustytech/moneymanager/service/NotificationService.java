package com.rustytech.moneymanager.service;

import com.rustytech.moneymanager.dtos.ExpenseDto;
import com.rustytech.moneymanager.entity.ExpenseEntity;
import com.rustytech.moneymanager.entity.Profile;
import com.rustytech.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    //@Scheduled(cron = "0 * * * * *", zone = "IST")
    @Scheduled(cron = "0 0 22 * * *", zone = "IST")
    public void senDailyIncomeExpenseReminder(){
        log.info("Job started: sendDailyIncomeExpenseReminder()");
        var profiles = profileRepository.findAll();
        for(Profile profile : profiles){
            String body = String.format(
                    """
                    <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                            <p>Hi %s,</p>
                            <p>
                                This is a friendly reminder to add your income and expenses for today in Money Manager.
                            </p>
                            <p>
                                <a href="%s"
                                   style="display:inline-block; padding:10px 20px; background-color:#4CAF50;
                                          color:white; text-decoration:none; border-radius:5px;">
                                    Open Money Manager
                                </a>
                            </p>
                            <p>Best regards,<br>Money Manager Team</p>
                        </body>
                    </html>
                    """,
                    profile.getFullName(),
                    frontendUrl
            );
            emailService.senEmailWithMimeMessage(profile.getEmail(), "Daily reminder: Add your Income and Expense", body);
        }
        log.info("Job completed: sendDailyIncomeExpenseReminder()");

    }

    //@Scheduled(cron = "0 * * * * *", zone = "IST")
    @Scheduled(cron = "0 0 23 * * *", zone = "IST")
    public void sendDailyExpenseSummary() {
        log.info("Job started: sendDailyExpenseSummary()");
        var profiles = profileRepository.findAll();

        for (Profile profile : profiles) {
            List<ExpenseDto> expenseList = expenseService.getExpensesForUserOnDate(
                    profile.getId(),
                    LocalDate.now(ZoneId.of("Europe/Berlin"))
            );

            if (!expenseList.isEmpty()) {
                log.info("Sending expense summary for user id: " + profile.getId());
                StringBuilder tableBuilder = new StringBuilder();
                tableBuilder.append("""
                <table style='border-collapse: collapse; width: 100%; font-family: Arial, sans-serif;'>
                    <thead>
                        <tr style='background-color: #4CAF50; color: white; text-align: left;'>
                            <th style='padding: 8px; border: 1px solid #ddd;'>#</th>
                            <th style='padding: 8px; border: 1px solid #ddd;'>Name</th>
                            <th style='padding: 8px; border: 1px solid #ddd;'>Category</th>
                            <th style='padding: 8px; border: 1px solid #ddd;'>Amount</th>
                            <th style='padding: 8px; border: 1px solid #ddd;'>Date</th>
                        </tr>
                    </thead>
                    <tbody>
            """);

                int index = 1;
                for (ExpenseDto expense : expenseList) {
                    tableBuilder.append(String.format("""
                    <tr style='background-color: %s;'>
                        <td style='padding: 8px; border: 1px solid #ddd;'>%d</td>
                        <td style='padding: 8px; border: 1px solid #ddd;'>%s</td>
                        <td style='padding: 8px; border: 1px solid #ddd;'>%s</td>
                        <td style='padding: 8px; border: 1px solid #ddd;'>%.2f</td>
                        <td style='padding: 8px; border: 1px solid #ddd;'>%s</td>
                    </tr>
                """,
                            (index % 2 == 0) ? "#f2f2f2" : "#ffffff", // alternance de couleurs
                            index++,
                            expense.getName(),
                            expense.getCategoryName(),
                            expense.getAmount(),
                            expense.getDate()
                    ));
                }

                tableBuilder.append("</tbody></table>");

                String body = String.format("""
                <html>
                    <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                        <p>Hi %s,</p>
                        <p>Here is your expense summary for today:</p>
                        %s
                        <p style="margin-top:20px;">Best regards,<br>Money Manager Team</p>
                    </body>
                </html>
            """, profile.getFullName(), tableBuilder);

                emailService.senEmailWithMimeMessage(
                        profile.getEmail(),
                        "Daily Expense Summary",
                        body
                );
            }
        }
        log.info("Job finished: sendDailyExpenseSummary()");
    }



}
