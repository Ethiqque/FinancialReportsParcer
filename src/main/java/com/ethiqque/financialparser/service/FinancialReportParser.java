package com.ethiqque.financialparser.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FinancialReportParser {

    public Map<String, Object> parsePdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            log.info("Loaded PDF document");

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Extracted PDF Text");

            String[] pages = text.split("Apple Inc. | 2023 Form 10-K");

            return extractFinancialData(pages);
        } catch (IOException e) {
            log.error("Error: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> extractFinancialData(String[] pages) {
        Map<String, Object> data = new HashMap<>();

        if (pages == null || pages.length == 0) {
            log.info("PDF text is empty, returning no data.");
        } else {
            data.put("Assets", extractAssets(pages));
            data.put("Liabilities_and_Shareholders_Equity", extractLiabilitiesAndEquity(pages));
            data.put("Income_Statement", extractIncomeStatement(pages));
            data.put("Earnings_Per_Share", extractEarningsPerShare(pages));
            data.put("Shares_Used_In_Computing_EPS", extractSharesUsedInComputingEPS(pages));
            data.put("Comprehensive_Income_Statement", extractComprehensiveIncomeStatement(pages));
            data.put("Shareholders_Equity_Statement", extractShareholdersEquityStatement(pages));
            data.put("Cash_Flow_Statement", extractCashFlowStatement(pages));
            data.put("Net_Sales_And_EPS", extractNetSalesAndEPS(pages));
            data.put("Cash_Equivalents_And_Marketable_Securities", extractCashEquivalentsAndMarketableSecurities(pages));
            data.put("Debt_Securities_And_Derivatives", extractDebtSecuritiesAndDerivatives(pages));
            data.put("Hedged_Assets_And_Liabilities", extractHedgedAssetsAndLiabilities(pages));
            data.put("Property_Plant_And_Equipment_And_Other_Details", extractPropertyPlantAndEquipmentAndOtherDetails(pages));
            data.put("Income_Taxes", extractIncomeTaxes(pages));
            data.put("Deferred_Tax_And_Uncertain_Positions", extractDeferredTaxAndUncertainPositions(pages));
            data.put("Leases", extractLeases(pages));
            data.put("Commercial_Paper", extractCommercialPaper(pages));
            data.put("Lease_Liability_Maturities", extractLeaseLiabilityMaturities(pages));
            data.put("Term_Debt", extractTermDebt(pages));
            data.put("Common_Stock", extractCommonStock(pages));
            data.put("Share_Based_Compensation_And_Purchase_Obligations", extractShareBasedCompensationAndPurchaseObligations(pages));
            data.put("Segment_Information_And_Geographic_Data", extractSegmentInformationAndGeographicData(pages));
            data.put("Net_Sales_And_Long_Lived_Assets", extractNetSalesAndLongLivedAssets(pages));
        }

        return data;
    }

    private String findPageWithText(String[] pages, String keyword) {
        for (String page : pages) {
            if (page.contains(keyword)) {
                return page;
            }
        }
        return null;
    }

    private double parseFieldForYear(String text, String fieldName) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().contains(fieldName)) {
                log.info("Field found: {}", fieldName);
                log.info("Extracted Line: {}", line);

                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    if (token.matches("\\(.*?\\)") || token.matches("-?\\d+(,\\d{3})*(\\.\\d+)?")) {
                        return parseNumericString(token);
                    }
                }
            }
        }
        log.info("Field not found: {}", fieldName);
        return 0.0;
    }

    private double parseNumericString(String numericString) {
        try {
            if (numericString.startsWith("(") && numericString.endsWith(")")) {
                numericString = numericString.replace("(", "-").replace(")", "");
            }

            return Double.parseDouble(numericString.replace(",", ""));
        } catch (NumberFormatException e) {
            log.error("Error parsing numeric value: {}", numericString, e);
            return 0.0;
        }
    }

    private double parseSegmentData(String text, String segment, String label) {
        String pattern = segment + ":[\\s\\S]*?" + label + "\\s*\\$\\s*([0-9,]+)";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(text);

        if (matcher.find()) {
            String value = matcher.group(1);
            return parseNumericString(value);
        } else {
            log.info("Data not found for segment: {} and label: {}", segment, label);
            return 0.0;
        }
    }

    private double parseField(String text, String fieldName) {
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().contains(fieldName)) {
                String line = lines[i].replace(fieldName, "").trim();

                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    if (token.matches("-?\\d+(,\\d{3})*(\\.\\d+)?")) {
                        try {
                            return Double.parseDouble(token.replace(",", ""));
                        } catch (NumberFormatException e) {
                            log.error("Failed to parse number from token: " + token, e);
                        }
                    }
                }
            }
        }
        return 0.0;
    }

    //1
    private Map<String, Object> extractIncomeStatement(String[] pages) {
        Map<String, Object> incomeStatement = new HashMap<>();
        String rightPage = findPageWithText(pages, "CONSOLIDATED STATEMENTS OF OPERATIONS");

        if (rightPage != null) {
            incomeStatement.put("Products_Net_Sales", parseFieldForYear(rightPage, "Products"));
            incomeStatement.put("Services_Net_Sales", parseFieldForYear(rightPage, "Services"));
            incomeStatement.put("Total_Net_Sales", parseFieldForYear(rightPage, "Total net sales"));
            incomeStatement.put("Products_Cost_of_Sales", parseFieldForYear(rightPage, "Products"));
            incomeStatement.put("Services_Cost_of_Sales", parseFieldForYear(rightPage, "Services"));
            incomeStatement.put("Total_Cost_of_Sales", parseFieldForYear(rightPage, "Total cost of sales"));
            incomeStatement.put("Gross_Margin", parseFieldForYear(rightPage, "Gross margin"));
            incomeStatement.put("Research_and_Development", parseFieldForYear(rightPage, "Research and development"));
            incomeStatement.put("Selling_General_and_Administrative", parseFieldForYear(rightPage, "Selling, general and administrative"));
            incomeStatement.put("Total_Operating_Expenses", parseFieldForYear(rightPage, "Total operating expenses"));
            incomeStatement.put("Operating_Income", parseFieldForYear(rightPage, "Operating income"));
            incomeStatement.put("Other_Income_Expense", parseFieldForYear(rightPage, "Other income/(expense), net"));
            incomeStatement.put("Income_Before_Taxes", parseFieldForYear(rightPage, "Income before provision for income taxes"));
            incomeStatement.put("Provision_for_Income_Taxes", parseFieldForYear(rightPage, "Provision for income taxes"));
            incomeStatement.put("Net_Income", parseFieldForYear(rightPage, "Net income"));
        }

        return incomeStatement;
    }

    //1
    private Map<String, Object> extractEarningsPerShare(String[] pages) {
        Map<String, Object> earningsPerShare = new HashMap<>();
        String rightPage = findPageWithText(pages, "CONSOLIDATED STATEMENTS OF OPERATIONS");

        if (rightPage != null) {
            earningsPerShare.put("Basic_EPS", parseFieldForYear(rightPage, "Basic"));
            earningsPerShare.put("Diluted_EPS", parseFieldForYear(rightPage, "Diluted"));
        }

        return earningsPerShare;
    }

    //1
    private Map<String, Object> extractSharesUsedInComputingEPS(String[] pages) {
        Map<String, Object> sharesUsed = new HashMap<>();

        String rightPage = findPageWithText(pages, "CONSOLIDATED STATEMENTS OF OPERATIONS");

        if (rightPage != null) {
            sharesUsed.put("Basic_Shares", parseFieldForYear(rightPage, "Basic"));
            sharesUsed.put("Diluted_Shares", parseFieldForYear(rightPage, "Diluted"));
        }

        return sharesUsed;
    }

    //2
    private Map<String, Double> extractComprehensiveIncomeStatement(String[] pages) {
        Map<String, Double> comprehensiveIncome = new HashMap<>();

        String rightPage = findPageWithText(pages, "CONSOLIDATED STATEMENTS OF COMPREHENSIVE INCOME");

        if (rightPage != null) {
            comprehensiveIncome.put("Net_Income", parseFieldForYear(rightPage, "Net income"));
            comprehensiveIncome.put("Change_in_Foreign_Currency_Translation", parseFieldForYear(rightPage, "Change in foreign currency translation"));

            comprehensiveIncome.put("Change_in_Fair_Value_of_Derivative_Instruments", parseFieldForYear(rightPage, "Change in fair value of derivative instruments"));
            comprehensiveIncome.put("Adjustment_for_Net_Gains_Losses_on_Derivative_Instruments", parseFieldForYear(rightPage, "Adjustment for net (gains)/losses realized and included in net"));
            comprehensiveIncome.put("Total_Change_in_Unrealized_Gains_Losses_on_Derivative_Instruments", parseFieldForYear(rightPage, "Total change in unrealized gains/losses on derivative instruments"));

            comprehensiveIncome.put("Change_in_Fair_Value_of_Marketable_Debt_Securities", parseFieldForYear(rightPage, "Change in fair value of marketable debt securities"));
            comprehensiveIncome.put("Adjustment_for_Net_Gains_Losses_on_Marketable_Debt_Securities", parseFieldForYear(rightPage, "Adjustment for net (gains)/losses realized and included in net"));
            comprehensiveIncome.put("Total_Change_in_Unrealized_Gains_Losses_on_Marketable_Debt_Securities", parseFieldForYear(rightPage, "Total change in unrealized gains/losses on marketable debt securities"));

            comprehensiveIncome.put("Total_Other_Comprehensive_Income_Loss", parseFieldForYear(rightPage, "Total other comprehensive income/(loss)"));
            comprehensiveIncome.put("Total_Comprehensive_Income", parseFieldForYear(rightPage, "Total comprehensive income"));
        }
        return comprehensiveIncome;
    }


    //3
    private Map<String, Object> extractAssets(String[] pages) {
        Map<String, Object> assets = new HashMap<>();

        String rightPage = findPageWithText(pages, "CONSOLIDATED BALANCE SHEETS");
        if (rightPage != null) {
            Map<String, Object> currentAssets = new HashMap<>();
            currentAssets.put("Cash_and_Cash_Equivalents", parseFieldForYear(rightPage, "Cash and cash equivalents"));
            currentAssets.put("Marketable_Securities", parseFieldForYear(rightPage, "Marketable securities"));
            currentAssets.put("Accounts_Receivable_Net", parseFieldForYear(rightPage, "Accounts receivable, net"));
            currentAssets.put("Vendor_Non_Trade_Receivables", parseFieldForYear(rightPage, "Vendor non-trade receivables"));
            currentAssets.put("Inventories", parseFieldForYear(rightPage, "Inventories"));
            currentAssets.put("Other_Current_Assets", parseFieldForYear(rightPage, "Other current assets"));
            currentAssets.put("Total_Current_Assets", parseFieldForYear(rightPage, "Total current assets"));
            assets.put("Current_Assets", currentAssets);

            Map<String, Object> nonCurrentAssets = new HashMap<>();
            nonCurrentAssets.put("Marketable_Securities", parseFieldForYear(rightPage, "Marketable securities"));
            nonCurrentAssets.put("Property_Plant_and_Equipment_Net", parseFieldForYear(rightPage, "Property, plant and equipment, net"));
            nonCurrentAssets.put("Other_Non_Current_Assets", parseFieldForYear(rightPage, "Other non-current assets"));
            nonCurrentAssets.put("Total_Non_Current_Assets", parseFieldForYear(rightPage, "Total non-current assets"));
            assets.put("Non_Current_Assets", nonCurrentAssets);

            assets.put("Total_Assets", parseFieldForYear(rightPage, "Total assets"));
        }

        return assets;
    }

    //4
    private Map<String, Object> extractLiabilitiesAndEquity(String[] pages) {
        Map<String, Object> liabilitiesAndEquity = new HashMap<>();

        String rightPage = findPageWithText(pages, "LIABILITIES AND SHAREHOLDERS’ EQUITY:");

        if (rightPage != null) {
            Map<String, Object> currentLiabilities = new HashMap<>();
            currentLiabilities.put("Accounts_Payable", parseFieldForYear(rightPage, "Accounts payable"));
            currentLiabilities.put("Other_Current_Liabilities", parseFieldForYear(rightPage, "Other current liabilities"));
            currentLiabilities.put("Deferred_Revenue", parseFieldForYear(rightPage, "Deferred revenue"));
            currentLiabilities.put("Commercial_Paper", parseFieldForYear(rightPage, "Commercial paper"));
            currentLiabilities.put("Term_Debt", parseFieldForYear(rightPage, "Term debt"));
            currentLiabilities.put("Total_Current_Liabilities", parseFieldForYear(rightPage, "Total current liabilities"));
            liabilitiesAndEquity.put("Current_Liabilities", currentLiabilities);

            Map<String, Object> nonCurrentLiabilities = new HashMap<>();
            nonCurrentLiabilities.put("Term_Debt", parseFieldForYear(rightPage, "Term debt"));
            nonCurrentLiabilities.put("Other_Non_Current_Liabilities", parseFieldForYear(rightPage, "Other non-current liabilities"));
            nonCurrentLiabilities.put("Total_Non_Current_Liabilities", parseFieldForYear(rightPage, "Total non-current liabilities"));
            liabilitiesAndEquity.put("Non_Current_Liabilities", nonCurrentLiabilities);

            liabilitiesAndEquity.put("Total_Liabilities", parseFieldForYear(rightPage, "Total liabilities"));

            Map<String, Object> shareholdersEquity = new HashMap<>();
            shareholdersEquity.put("Common_Stock_and_Additional_Paid_In_Capital", parseFieldForYear(rightPage, "Common stock and additional paid-in capital"));
            shareholdersEquity.put("Accumulated_Deficit", parseFieldForYear(rightPage, "Accumulated deficit"));
            shareholdersEquity.put("Accumulated_Other_Comprehensive_Loss", parseFieldForYear(rightPage, "Accumulated other comprehensive loss"));
            shareholdersEquity.put("Total_Shareholders_Equity", parseFieldForYear(rightPage, "Total shareholders’ equity"));
            liabilitiesAndEquity.put("Shareholders_Equity", shareholdersEquity);

            liabilitiesAndEquity.put("Total_Liabilities_and_Shareholders_Equity", parseFieldForYear(rightPage, "Total liabilities and shareholders’ equity"));
        }

        return liabilitiesAndEquity;
    }

    //5 
    private Map<String, Object> extractShareholdersEquityStatement(String[] pages) {
        Map<String, Object> shareholdersEquity = new HashMap<>();

        String rightPage = findPageWithText(pages, "CONSOLIDATED STATEMENTS OF SHAREHOLDERS’ EQUITY");

        if (rightPage != null) {
            shareholdersEquity.put("Beginning_Balances_Total_Shareholders_Equity", parseFieldForYear(rightPage, "Total shareholders’ equity, beginning balances"));
            shareholdersEquity.put("Common_Stock_And_Additional_Paid_In_Capital_Beginning_Balances", parseFieldForYear(rightPage, "Beginning balances"));
            shareholdersEquity.put("Common_Stock_Issued", parseFieldForYear(rightPage, "Common stock issued"));
            shareholdersEquity.put("Share_Based_Compensation", parseFieldForYear(rightPage, "Share-based compensation"));
            shareholdersEquity.put("Ending_Balances_Common_Stock_And_Additional_Paid_In_Capital", parseFieldForYear(rightPage, "Ending balances"));

            shareholdersEquity.put("Net_Income", parseFieldForYear(rightPage, "Net income"));
            shareholdersEquity.put("Dividends_Declared", parseFieldForYear(rightPage, "Dividends and dividend equivalents declared"));
            shareholdersEquity.put("Common_Stock_Repurchased", parseFieldForYear(rightPage, "Common stock repurchased"));
            shareholdersEquity.put("Ending_Balances_Retained_Earnings", parseFieldForYear(rightPage, "Ending balances"));

            shareholdersEquity.put("Other_Comprehensive_Income_Loss", parseFieldForYear(rightPage, "Other comprehensive income/(loss)"));
            shareholdersEquity.put("Ending_Balances_Accumulated_Other_Comprehensive_Income", parseFieldForYear(rightPage, "Ending balances"));

            shareholdersEquity.put("Ending_Balances_Total_Shareholders_Equity", parseFieldForYear(rightPage, "Total shareholders’ equity, ending balances"));
            shareholdersEquity.put("Dividends_Per_Share", parseFieldForYear(rightPage, "Dividends and dividend equivalents declared per share or RSU"));
        }
        return shareholdersEquity;
    }

    //6
    private Map<String, Object> extractCashFlowStatement(String[] pages) {
        Map<String, Object> cashFlowStatement = new HashMap<>();

        String rightPage = findPageWithText(pages, "CONSOLIDATED STATEMENTS OF CASH FLOWS");
        if (rightPage != null) {
            cashFlowStatement.put("Beginning_Balances_Cash_Cash_Equivalents_and_Restricted_Cash", parseFieldForYear(rightPage, "Cash, cash equivalents and restricted cash, beginning balances"));

            Map<String, Object> operatingActivities = new HashMap<>();
            operatingActivities.put("Net_Income", parseFieldForYear(rightPage, "Net income"));
            operatingActivities.put("Depreciation_and_Amortization", parseFieldForYear(rightPage, "Depreciation and amortization"));
            operatingActivities.put("Share_Based_Compensation_Expense", parseFieldForYear(rightPage, "Share-based compensation expense"));
            operatingActivities.put("Other_Adjustments", parseFieldForYear(rightPage, "Other"));
            operatingActivities.put("Accounts_Receivable_Net", parseFieldForYear(rightPage, "Accounts receivable, net"));
            operatingActivities.put("Vendor_Non_Trade_Receivables", parseFieldForYear(rightPage, "Vendor non-trade receivables"));
            operatingActivities.put("Inventories", parseFieldForYear(rightPage, "Inventories"));
            operatingActivities.put("Other_Assets_and_Liabilities", parseFieldForYear(rightPage, "Other current and non-current assets"));
            operatingActivities.put("Accounts_Payable", parseFieldForYear(rightPage, "Accounts payable"));
            operatingActivities.put("Other_Liabilities", parseFieldForYear(rightPage, "Other current and non-current liabilities"));
            operatingActivities.put("Cash_Generated_by_Operating_Activities", parseFieldForYear(rightPage, "Cash generated by operating activities"));
            cashFlowStatement.put("Operating_Activities", operatingActivities);

            Map<String, Object> investingActivities = new HashMap<>();
            investingActivities.put("Purchases_of_Marketable_Securities", parseFieldForYear(rightPage, "Purchases of marketable securities"));
            investingActivities.put("Proceeds_from_Maturities_of_Marketable_Securities", parseFieldForYear(rightPage, "Proceeds from maturities of marketable securities"));
            investingActivities.put("Proceeds_from_Sales_of_Marketable_Securities", parseFieldForYear(rightPage, "Proceeds from sales of marketable securities"));
            investingActivities.put("Payments_for_Acquisition_of_Property_Plant_and_Equipment", parseFieldForYear(rightPage, "Payments for acquisition of property, plant and equipment"));
            investingActivities.put("Other_Investing_Activities", parseFieldForYear(rightPage, "Other"));
            investingActivities.put("Cash_Generated_by_Investing_Activities", parseFieldForYear(rightPage, "Cash generated by/(used in) investing activities"));
            cashFlowStatement.put("Investing_Activities", investingActivities);

            Map<String, Object> financingActivities = new HashMap<>();
            financingActivities.put("Payments_for_Taxes_Net_Share_Settlement_of_Equity_Awards", parseFieldForYear(rightPage, "Payments for taxes related to net share settlement of equity awards"));
            financingActivities.put("Payments_for_Dividends", parseFieldForYear(rightPage, "Payments for dividends and dividend equivalents"));
            financingActivities.put("Repurchases_of_Common_Stock", parseFieldForYear(rightPage, "Repurchases of common stock"));
            financingActivities.put("Proceeds_from_Issuance_of_Term_Debt_Net", parseFieldForYear(rightPage, "Proceeds from issuance of term debt, net"));
            financingActivities.put("Repayments_of_Term_Debt", parseFieldForYear(rightPage, "Repayments of term debt"));
            financingActivities.put("Proceeds_Repayments_of_Commercial_Paper_Net", parseFieldForYear(rightPage, "Proceeds from/(Repayments of) commercial paper, net"));
            financingActivities.put("Other_Financing_Activities", parseFieldForYear(rightPage, "Other"));
            financingActivities.put("Cash_Used_in_Financing_Activities", parseFieldForYear(rightPage, "Cash used in financing activities"));
            cashFlowStatement.put("Financing_Activities", financingActivities);

            cashFlowStatement.put("Ending_Balances_Cash_Cash_Equivalents_and_Restricted_Cash", parseFieldForYear(rightPage, "Cash, cash equivalents and restricted cash, ending balances"));

            Map<String, Object> supplementalCashFlowDisclosures = new HashMap<>();
            supplementalCashFlowDisclosures.put("Cash_Paid_for_Income_Taxes_Net", parseFieldForYear(rightPage, "Cash paid for income taxes, net"));
            supplementalCashFlowDisclosures.put("Cash_Paid_for_Interest", parseFieldForYear(rightPage, "Cash paid for interest"));
            cashFlowStatement.put("Supplemental_Cash_Flow_Disclosures", supplementalCashFlowDisclosures);
        }
        return cashFlowStatement;
    }

    //7
    private Map<String, Object> extractNetSalesAndEPS(String[] pages) {
        Map<String, Object> netSalesAndEPS = new HashMap<>();

        String rightPage = findPageWithText(pages, "Note 3 – Earnings Per Share");

        if (rightPage != null) {
            Map<String, Object> netSales = new HashMap<>();
            netSales.put("iPhone", parseFieldForYear(rightPage, "iPhone"));
            netSales.put("Mac", parseFieldForYear(rightPage, "Mac"));
            netSales.put("iPad", parseFieldForYear(rightPage, "iPad"));
            netSales.put("Wearables_Home_and_Accessories", parseFieldForYear(rightPage, "Wearables, Home and Accessories"));
            netSales.put("Services", parseFieldForYear(rightPage, "Services"));
            netSales.put("Total_Net_Sales", parseFieldForYear(rightPage, "Total net sales"));
            netSalesAndEPS.put("Net_Sales", netSales);

            Map<String, Object> eps = new HashMap<>();
            eps.put("Net_Income", parseFieldForYear(rightPage, "Net income"));
            eps.put("Basic_Earnings_Per_Share", parseFieldForYear(rightPage, "Basic earnings per share"));
            eps.put("Diluted_Earnings_Per_Share", parseFieldForYear(rightPage, "Diluted earnings per share"));
            eps.put("Weighted_Average_Basic_Shares_Outstanding", parseFieldForYear(rightPage, "Weighted-average basic shares outstanding"));
            eps.put("Effect_of_Dilutive_Share_Based_Awards", parseFieldForYear(rightPage, "Effect of dilutive share-based awards"));
            eps.put("Weighted_Average_Diluted_Shares", parseFieldForYear(rightPage, "Weighted-average diluted shares"));
            netSalesAndEPS.put("Earnings_Per_Share", eps);
        }
        return netSalesAndEPS;
    }

    //8
    private Map<String, Object> extractCashEquivalentsAndMarketableSecurities(String[] pages) {
        Map<String, Object> cashEquivalentsAndMarketableSecurities = new HashMap<>();

        String rightPage = findPageWithText(pages, "Cash, Cash Equivalents and Marketable Securities");

        if (rightPage != null) {
            Map<String, Object> level1_2023 = new HashMap<>();
            level1_2023.put("Money_Market_Funds", parseFieldForYear(rightPage, "Money market funds"));
            level1_2023.put("Mutual_Funds_Equity_Securities", parseFieldForYear(rightPage, "Mutual funds and equity securities"));
            cashEquivalentsAndMarketableSecurities.put("Level_1", level1_2023);

            Map<String, Object> level2_2023 = new HashMap<>();
            level2_2023.put("U.S_Treasury_Securities", parseFieldForYear(rightPage, "U.S. Treasury securities"));
            level2_2023.put("U.S_Agency_Securities", parseFieldForYear(rightPage, "U.S. agency securities"));
            level2_2023.put("Non_U.S_Government_Securities", parseFieldForYear(rightPage, "Non-U.S. government securities"));
            level2_2023.put("Certificates_of_Deposit_and_Time_Deposits", parseFieldForYear(rightPage, "Certificates of deposit and time deposits"));
            level2_2023.put("Commercial_Paper", parseFieldForYear(rightPage, "Commercial paper"));
            level2_2023.put("Corporate_Debt_Securities", parseFieldForYear(rightPage, "Corporate debt securities"));
            level2_2023.put("Municipal_Securities", parseFieldForYear(rightPage, "Municipal securities"));
            level2_2023.put("Mortgage_and_Asset_Backed_Securities", parseFieldForYear(rightPage, "Mortgage- and asset-backed securities"));
            cashEquivalentsAndMarketableSecurities.put("Level_2", level2_2023);
        }
        return cashEquivalentsAndMarketableSecurities;
    }

    //9
    private Map<String, Object> extractDebtSecuritiesAndDerivatives(String[] pages) {
        Map<String, Object> debtSecuritiesAndDerivatives = new HashMap<>();

        String rightPage = findPageWithText(pages, "Derivative Instruments and Hedging");

        if (rightPage != null) {
            Map<String, Object> nonCurrentDebtSecurities = new HashMap<>();
            nonCurrentDebtSecurities.put("Due_After_1_Year_Through_5_Years", parseField(rightPage, "Due after 1 year through 5 years"));
            nonCurrentDebtSecurities.put("Due_After_5_Years_Through_10_Years", parseField(rightPage, "Due after 5 years through 10 years"));
            nonCurrentDebtSecurities.put("Due_After_10_Years", parseField(rightPage, "Due after 10 years"));
            nonCurrentDebtSecurities.put("Total_Fair_Value", parseField(rightPage, "Total fair value"));
            debtSecuritiesAndDerivatives.put("Non_Current_Marketable_Debt_Securities", nonCurrentDebtSecurities);

            Map<String, Object> derivativeInstruments = new HashMap<>();

            Map<String, Object> accountingHedges = new HashMap<>();
            accountingHedges.put("Foreign_Exchange_Contracts", parseField(rightPage, "Foreign exchange contracts"));
            accountingHedges.put("Interest_Rate_Contracts", parseField(rightPage, "Interest rate contracts"));
            derivativeInstruments.put("Accounting_Hedges", accountingHedges);

            Map<String, Object> nonAccountingHedges = new HashMap<>();
            nonAccountingHedges.put("Foreign_Exchange_Contracts", parseField(rightPage, "Foreign exchange contracts"));
            derivativeInstruments.put("Non_Accounting_Hedges", nonAccountingHedges);

            debtSecuritiesAndDerivatives.put("Derivative_Instruments", derivativeInstruments);
        }
        return debtSecuritiesAndDerivatives;
    }

    //10
    private Map<String, Object> extractHedgedAssetsAndLiabilities(String[] pages) {
        Map<String, Object> hedgedAssetsAndLiabilities = new HashMap<>();

        String rightPage = findPageWithText(pages, "Accounts Receivable");

        if (rightPage != null) {
            hedgedAssetsAndLiabilities.put("Marketable_Securities", parseFieldForYear(rightPage, "Current and non-current marketable securities"));
            hedgedAssetsAndLiabilities.put("Term_Debt", parseFieldForYear(rightPage, "Current and non-current term debt"));
        }
        return hedgedAssetsAndLiabilities;
    }

    //11
    private Map<String, Object> extractPropertyPlantAndEquipmentAndOtherDetails(String[] pages) {
        Map<String, Object> propertyAndFinancialDetails = new HashMap<>();

        String rightPage = findPageWithText(pages, "Note 5 – Property, Plant and Equipment");

        if (rightPage != null) {
            Map<String, Object> propertyPlantAndEquipment = new HashMap<>();
            propertyPlantAndEquipment.put("Land_and_Buildings", parseFieldForYear(rightPage, "Land and buildings"));
            propertyPlantAndEquipment.put("Machinery_Equipment_Internal_Use_Software", parseFieldForYear(rightPage, "Machinery, equipment and internal-use software"));
            propertyPlantAndEquipment.put("Leasehold_Improvements", parseFieldForYear(rightPage, "Leasehold improvements"));
            propertyPlantAndEquipment.put("Gross_Property_Plant_And_Equipment", parseFieldForYear(rightPage, "Gross property, plant and equipment"));
            propertyPlantAndEquipment.put("Accumulated_Depreciation", parseFieldForYear(rightPage, "Accumulated depreciation"));
            propertyPlantAndEquipment.put("Net_Property_Plant_And_Equipment", parseFieldForYear(rightPage, "Total property, plant and equipment, net"));
            propertyAndFinancialDetails.put("Property_Plant_And_Equipment", propertyPlantAndEquipment);

            propertyAndFinancialDetails.put("Deferred_Tax_Assets", parseFieldForYear(rightPage, "Deferred tax assets"));
            propertyAndFinancialDetails.put("Other_Non_Current_Assets", parseFieldForYear(rightPage, "Other non-current assets"));

            propertyAndFinancialDetails.put("Income_Taxes_Payable", parseFieldForYear(rightPage, "Income taxes payable"));
            propertyAndFinancialDetails.put("Other_Current_Liabilities", parseFieldForYear(rightPage, "Other current liabilities"));

            propertyAndFinancialDetails.put("Long_Term_Taxes_Payable", parseFieldForYear(rightPage, "Long-term taxes payable"));
            propertyAndFinancialDetails.put("Other_Non_Current_Liabilities", parseFieldForYear(rightPage, "Other non-current liabilities"));

            Map<String, Object> otherIncomeExpense = new HashMap<>();
            otherIncomeExpense.put("Interest_And_Dividend_Income", parseFieldForYear(rightPage, "Interest and dividend income"));
            otherIncomeExpense.put("Interest_Expense", parseFieldForYear(rightPage, "Interest expense"));
            otherIncomeExpense.put("Other_Income_Expense_Net", parseFieldForYear(rightPage, "Other income/(expense), net"));
            otherIncomeExpense.put("Total_Other_Income_Expense", parseFieldForYear(rightPage, "Total other income/(expense), net"));
            propertyAndFinancialDetails.put("Other_Income_Expense_Net", otherIncomeExpense);
        }

        return propertyAndFinancialDetails;
    }

    //12
    private Map<String, Object> extractIncomeTaxes(String[] pages) {
        Map<String, Object> incomeTaxes = new HashMap<>();

        String rightPage = findPageWithText(pages, "Note 7 – Income Taxes");

        if (rightPage != null) {
            Map<String, Object> federalTaxes = new HashMap<>();
            federalTaxes.put("Current", parseFieldForYear(rightPage, "Federal:\nCurrent"));
            federalTaxes.put("Deferred", parseFieldForYear(rightPage, "Federal:\nDeferred"));
            federalTaxes.put("Total", parseFieldForYear(rightPage, "Federal:\nTotal"));
            incomeTaxes.put("Federal_Taxes", federalTaxes);

            Map<String, Object> stateTaxes = new HashMap<>();
            stateTaxes.put("Current", parseFieldForYear(rightPage, "State:\nCurrent"));
            stateTaxes.put("Deferred", parseFieldForYear(rightPage, "State:\nDeferred"));
            stateTaxes.put("Total", parseFieldForYear(rightPage, "State:\nTotal"));
            incomeTaxes.put("State_Taxes", stateTaxes);

            Map<String, Object> foreignTaxes = new HashMap<>();
            foreignTaxes.put("Current", parseFieldForYear(rightPage, "Foreign:\nCurrent"));
            foreignTaxes.put("Deferred", parseFieldForYear(rightPage, "Foreign:\nDeferred"));
            foreignTaxes.put("Total", parseFieldForYear(rightPage, "Foreign:\nTotal"));
            incomeTaxes.put("Foreign_Taxes", foreignTaxes);

            incomeTaxes.put("Provision_For_Income_Taxes", parseFieldForYear(rightPage, "Provision for income taxes"));

            Map<String, Object> reconciliation = new HashMap<>();
            reconciliation.put("Computed_Expected_Tax", parseFieldForYear(rightPage, "Computed expected tax"));
            reconciliation.put("State_Taxes_Net_of_Federal_Effect", parseFieldForYear(rightPage, "State taxes, net of federal effect"));
            reconciliation.put("Earnings_of_Foreign_Subsidiaries", parseFieldForYear(rightPage, "Earnings of foreign subsidiaries"));
            reconciliation.put("Research_and_Development_Credit", parseFieldForYear(rightPage, "Research and development credit"));
            reconciliation.put("Excess_Tax_Benefits_from_Equity_Awards", parseFieldForYear(rightPage, "Excess tax benefits from equity awards"));
            reconciliation.put("Foreign_Derived_Intangible_Income_Deduction", parseFieldForYear(rightPage, "Foreign-derived intangible income deduction"));
            reconciliation.put("Other", parseFieldForYear(rightPage, "Other"));
            incomeTaxes.put("Reconciliation_of_Provision", reconciliation);

            incomeTaxes.put("Effective_Tax_Rate", parseFieldForYear(rightPage, "Effective tax rate"));
        }
        return incomeTaxes;
    }

    //13
    private Map<String, Object> extractDeferredTaxAndUncertainPositions(String[] pages) {
        Map<String, Object> deferredTaxAndUncertainPositions = new HashMap<>();

        String rightPage = findPageWithText(pages, "Deferred Tax Assets and Liabilities");

        if (rightPage != null) {
            Map<String, Object> deferredTaxAssets = new HashMap<>();
            deferredTaxAssets.put("Tax_Credit_Carryforwards", parseFieldForYear(rightPage, "Tax credit carryforwards"));
            deferredTaxAssets.put("Accrued_Liabilities_And_Other_Reserves", parseFieldForYear(rightPage, "Accrued liabilities and other reserves"));
            deferredTaxAssets.put("Capitalized_Research_And_Development", parseFieldForYear(rightPage, "Capitalized research and development"));
            deferredTaxAssets.put("Deferred_Revenue", parseFieldForYear(rightPage, "Deferred revenue"));
            deferredTaxAssets.put("Unrealized_Losses", parseFieldForYear(rightPage, "Unrealized losses"));
            deferredTaxAssets.put("Lease_Liabilities", parseFieldForYear(rightPage, "Lease liabilities"));
            deferredTaxAssets.put("Other", parseFieldForYear(rightPage, "Other"));
            deferredTaxAssets.put("Total_Deferred_Tax_Assets", parseFieldForYear(rightPage, "Total deferred tax assets"));
            deferredTaxAssets.put("Valuation_Allowance", parseFieldForYear(rightPage, "Valuation allowance"));
            deferredTaxAssets.put("Net_Deferred_Tax_Assets", parseFieldForYear(rightPage, "Total deferred tax assets, net"));

            deferredTaxAndUncertainPositions.put("Deferred_Tax_Assets", deferredTaxAssets);

            Map<String, Object> deferredTaxLiabilities = new HashMap<>();
            deferredTaxLiabilities.put("Right_Of_Use_Assets", parseFieldForYear(rightPage, "Right-of-use assets"));
            deferredTaxLiabilities.put("Depreciation", parseFieldForYear(rightPage, "Depreciation"));
            deferredTaxLiabilities.put("Minimum_Tax_On_Foreign_Earnings", parseFieldForYear(rightPage, "Minimum tax on foreign earnings"));
            deferredTaxLiabilities.put("Unrealized_Gains", parseFieldForYear(rightPage, "Unrealized gains"));
            deferredTaxLiabilities.put("Other", parseFieldForYear(rightPage, "Other"));
            deferredTaxLiabilities.put("Total_Deferred_Tax_Liabilities", parseFieldForYear(rightPage, "Total deferred tax liabilities"));

            deferredTaxAndUncertainPositions.put("Deferred_Tax_Liabilities", deferredTaxLiabilities);

            deferredTaxAndUncertainPositions.put("Net_Deferred_Tax_Assets", parseFieldForYear(rightPage, "Net deferred tax assets"));

            Map<String, Object> uncertainTaxPositions = new HashMap<>();
            uncertainTaxPositions.put("Gross_Unrecognized_Tax_Benefits", parseFieldForYear(rightPage, "total amount of gross unrecognized tax benefits"));
            uncertainTaxPositions.put("Impact_On_Effective_Tax_Rate_If_Recognized", parseFieldForYear(rightPage, "if recognized, would impact the Company’s effective tax rate"));
            uncertainTaxPositions.put("Decrease_In_Next_12_Months", parseFieldForYear(rightPage, "could decrease in the next 12 months"));

            Map<String, Object> changesInUnrecognizedTaxBenefits = new HashMap<>();
            changesInUnrecognizedTaxBenefits.put("Beginning_Balances", parseFieldForYear(rightPage, "Beginning balances"));
            changesInUnrecognizedTaxBenefits.put("Increases_Prior_Year_Tax_Positions", parseFieldForYear(rightPage, "Increases related to tax positions taken during a prior year"));
            changesInUnrecognizedTaxBenefits.put("Decreases_Prior_Year_Tax_Positions", parseFieldForYear(rightPage, "Decreases related to tax positions taken during a prior year"));
            changesInUnrecognizedTaxBenefits.put("Increases_Current_Year_Tax_Positions", parseFieldForYear(rightPage, "Increases related to tax positions taken during the current year"));
            changesInUnrecognizedTaxBenefits.put("Decreases_Settlements", parseFieldForYear(rightPage, "Decreases related to settlements with taxing authorities"));
            changesInUnrecognizedTaxBenefits.put("Decreases_Statute_Of_Limitations", parseFieldForYear(rightPage, "Decreases related to expiration of the statute of limitations"));
            changesInUnrecognizedTaxBenefits.put("Ending_Balances", parseFieldForYear(rightPage, "Ending balances"));

            uncertainTaxPositions.put("Changes_In_Gross_Unrecognized_Tax_Benefits", changesInUnrecognizedTaxBenefits);
            deferredTaxAndUncertainPositions.put("Uncertain_Tax_Positions", uncertainTaxPositions);
        }
        return deferredTaxAndUncertainPositions;
    }

    //14
    private Map<String, Object> extractLeases(String[] pages) {
        Map<String, Object> leasesData = new HashMap<>();

        String rightPage = findPageWithText(pages, "European Commission State Aid Decision");

        if (rightPage != null) {
            Map<String, Object> leaseCosts = new HashMap<>();
            leaseCosts.put("Fixed_Lease_Costs", parseFieldForYear(rightPage, "Lease costs associated with fixed payments on the Company’s operating leases"));
            leaseCosts.put("Variable_Lease_Costs", parseFieldForYear(rightPage, "Lease costs associated with variable payments on the Company’s leases"));
            leasesData.put("Lease_Costs", leaseCosts);

            leasesData.put("Fixed_Cash_Payments_For_Operating_Leases", parseFieldForYear(rightPage, "The Company made"));
            leasesData.put("Noncash_Activities_For_ROU_Assets", parseFieldForYear(rightPage, "Noncash activities involving right-of-use (\"ROU\") assets"));

            Map<String, Object> rouAssetsAndLeaseLiabilities = new HashMap<>();

            Map<String, Object> rightOfUseAssets = new HashMap<>();
            rightOfUseAssets.put("Operating_Leases_Other_Non_Current_Assets", parseFieldForYear(rightPage, "Operating leases Other non-current assets"));
            rightOfUseAssets.put("Finance_Leases_Property_Plant_And_Equipment_Net", parseFieldForYear(rightPage, "Finance leases Property, plant and equipment, net"));
            rightOfUseAssets.put("Total_ROU_Assets", parseFieldForYear(rightPage, "Total right-of-use assets"));
            rouAssetsAndLeaseLiabilities.put("Right_Of_Use_Assets", rightOfUseAssets);

            Map<String, Object> leaseLiabilities = new HashMap<>();
            leaseLiabilities.put("Operating_Leases_Other_Current_Liabilities", parseFieldForYear(rightPage, "Operating leases Other current liabilities"));
            leaseLiabilities.put("Operating_Leases_Other_Non_Current_Liabilities", parseFieldForYear(rightPage, "Other non-current liabilities"));
            leaseLiabilities.put("Finance_Leases_Other_Current_Liabilities", parseFieldForYear(rightPage, "Finance leases Other current liabilities"));
            leaseLiabilities.put("Finance_Leases_Other_Non_Current_Liabilities", parseFieldForYear(rightPage, "Other non-current liabilities"));
            leaseLiabilities.put("Total_Lease_Liabilities", parseFieldForYear(rightPage, "Total lease liabilities"));
            rouAssetsAndLeaseLiabilities.put("Lease_Liabilities", leaseLiabilities);

            leasesData.put("ROU_Assets_And_Lease_Liabilities", rouAssetsAndLeaseLiabilities);
        }
        return leasesData;
    }

    //15
    private Map<String, Object> extractCommercialPaper(String[] pages) {
        Map<String, Object> commercialPaper = new HashMap<>();

        String rightPage = findPageWithText(pages, "Note 9 – Debt");

        if (rightPage != null) {
            Map<String, Object> commercialPaperCashFlows = new HashMap<>();
            commercialPaperCashFlows.put("Maturities_90_Days_Or_Less_Proceeds_Repayments_Net", parseFieldForYear(rightPage, "Maturities 90 days or less:\nProceeds from/(Repayments of) commercial paper, net"));
            commercialPaperCashFlows.put("Maturities_Greater_Than_90_Days_Proceeds", parseFieldForYear(rightPage, "Proceeds from commercial paper"));
            commercialPaperCashFlows.put("Maturities_Greater_Than_90_Days_Repayments", parseFieldForYear(rightPage, "Repayments of commercial paper"));
            commercialPaperCashFlows.put("Maturities_Greater_Than_90_Days_Proceeds_Repayments_Net", parseFieldForYear(rightPage, "Proceeds from/(Repayments of) commercial paper, net"));
            commercialPaperCashFlows.put("Total_Proceeds_Repayments_Net", parseFieldForYear(rightPage, "Total proceeds from/(repayments of) commercial paper, net"));

            commercialPaper.put("Commercial_Paper_Cash_Flows", commercialPaperCashFlows);
        }
        return commercialPaper;
    }

    //16
    private Map<String, Object> extractLeaseLiabilityMaturities(String[] pages) {
        Map<String, Object> leaseMaturities = new HashMap<>();

        String rightPage = findPageWithText(pages, "Note 9 – Debt");

        if (rightPage != null) {
            Map<String, Object> operatingLeases = new HashMap<>();
            operatingLeases.put("2024", parseFieldForYear(rightPage, "2024"));
            operatingLeases.put("2025", parseFieldForYear(rightPage, "2025"));
            operatingLeases.put("2026", parseFieldForYear(rightPage, "2026"));
            operatingLeases.put("2027", parseFieldForYear(rightPage, "2027"));
            operatingLeases.put("2028", parseFieldForYear(rightPage, "2028"));
            operatingLeases.put("Thereafter", parseFieldForYear(rightPage, "Thereafter"));
            operatingLeases.put("Total_Undiscounted_Liabilities", parseFieldForYear(rightPage, "Total undiscounted liabilities"));
            operatingLeases.put("Imputed_Interest", parseFieldForYear(rightPage, "Less: Imputed interest"));
            operatingLeases.put("Total_Lease_Liabilities", parseFieldForYear(rightPage, "Total lease liabilities"));

            leaseMaturities.put("Operating_Leases", operatingLeases);

            Map<String, Object> financeLeases = new HashMap<>();
            financeLeases.put("2024", parseFieldForYear(rightPage, "Finance\nLeases Total"));
            financeLeases.put("2025", parseFieldForYear(rightPage, "2025"));
            financeLeases.put("2026", parseFieldForYear(rightPage, "2026"));
            financeLeases.put("2027", parseFieldForYear(rightPage, "2027"));
            financeLeases.put("2028", parseFieldForYear(rightPage, "2028"));
            financeLeases.put("Thereafter", parseFieldForYear(rightPage, "Thereafter"));
            financeLeases.put("Total_Undiscounted_Liabilities", parseFieldForYear(rightPage, "Total undiscounted liabilities"));
            financeLeases.put("Imputed_Interest", parseFieldForYear(rightPage, "Less: Imputed interest"));
            financeLeases.put("Total_Lease_Liabilities", parseFieldForYear(rightPage, "Total lease liabilities"));

            leaseMaturities.put("Finance_Leases", financeLeases);

            Map<String, Object> leaseTermAndDiscountRate = new HashMap<>();
            leaseTermAndDiscountRate.put("Weighted_Average_Remaining_Lease_Term", parseFieldForYear(rightPage, "The weighted-average remaining lease term"));
            leaseTermAndDiscountRate.put("Discount_Rate", parseFieldForYear(rightPage, "The discount rate related to the Company’s lease liabilities"));

            leaseMaturities.put("Lease_Term_And_Discount_Rate", leaseTermAndDiscountRate);

            leaseMaturities.put("Future_Payments_For_Additional_Leases", parseFieldForYear(rightPage, "the Company had $544 million of future payments"));
        }
        return leaseMaturities;
    }


    //17
    private Map<String, Object> extractTermDebt(String[] pages) {
        Map<String, Object> termDebtData = new HashMap<>();

        String rightPage = findPageWithText(pages, "Note 10 – Shareholders’ Equity");

        if (rightPage != null) {
            Map<String, Object> debtIssuances = new HashMap<>();
            debtIssuances.put("Fixed_Rate_Notes_2013_2022", parseFieldForYear(rightPage, "Fixed-rate 0.000% – 4.650% notes 2024 – 2062"));
            debtIssuances.put("Third_Quarter_2023_Issuance", parseFieldForYear(rightPage, "Fixed-rate 4.000% – 4.850% notes 2026 – 2053"));
            termDebtData.put("Debt_Issuances", debtIssuances);

            termDebtData.put("Total_Term_Debt_Principal", parseFieldForYear(rightPage, "Total term debt principal"));

            Map<String, Object> unamortizedAdjustments = new HashMap<>();
            unamortizedAdjustments.put("Unamortized_Premium_Discount_And_Issuance_Costs", parseFieldForYear(rightPage, "Unamortized premium/(discount) and issuance costs, net"));
            unamortizedAdjustments.put("Hedge_Accounting_Fair_Value_Adjustments", parseFieldForYear(rightPage, "Hedge accounting fair value adjustments"));
            termDebtData.put("Unamortized_Adjustments", unamortizedAdjustments);

            termDebtData.put("Current_Portion_Term_Debt", parseFieldForYear(rightPage, "Less: Current portion of term debt"));
            termDebtData.put("Non_Current_Portion_Term_Debt", parseFieldForYear(rightPage, "Total non-current portion of term debt"));

            Map<String, Object> futurePrincipalPayments = new HashMap<>();
            futurePrincipalPayments.put("2024", parseFieldForYear(rightPage, "2024"));
            futurePrincipalPayments.put("2025", parseFieldForYear(rightPage, "2025"));
            futurePrincipalPayments.put("2026", parseFieldForYear(rightPage, "2026"));
            futurePrincipalPayments.put("2027", parseFieldForYear(rightPage, "2027"));
            futurePrincipalPayments.put("2028", parseFieldForYear(rightPage, "2028"));
            futurePrincipalPayments.put("Thereafter", parseFieldForYear(rightPage, "Thereafter"));
            termDebtData.put("Future_Principal_Payments", futurePrincipalPayments);
        }

        return termDebtData;
    }

    //18
    private Map<String, Object> extractCommonStock(String[] pages) {

        Map<String, Object> commonStockChanges = new HashMap<>();
        String rightPage = findPageWithText(pages, "Note 11 – Share-Based Compensation");

        if (rightPage != null) {
            commonStockChanges.put("Common_Stock_Beginning_Balance_2023", parseFieldForYear(rightPage, "Common stock outstanding, beginning balances"));
            commonStockChanges.put("Common_Stock_Repurchased_2023", parseFieldForYear(rightPage, "Common stock repurchased"));
            commonStockChanges.put("Common_Stock_Issued_2023", parseFieldForYear(rightPage, "Common stock issued, net of shares withheld for employee taxes"));
            commonStockChanges.put("Common_Stock_Ending_Balance_2023", parseFieldForYear(rightPage, "Common stock outstanding, ending balances"));
        }
        return commonStockChanges;
    }

    //19
    private Map<String, Object> extractShareBasedCompensationAndPurchaseObligations(String[] pages) {
        Map<String, Object> data = new HashMap<>();

        String rightPage = findPageWithText(pages, "Note 12 – Commitments, Contingencies and Supply Concentrations");

        if (rightPage != null) {
            Map<String, Object> shareBasedCompensation = new HashMap<>();
            shareBasedCompensation.put("Share_Based_Compensation_Expense_2023", parseFieldForYear(rightPage, "Share-based compensation expense"));
            shareBasedCompensation.put("Income_Tax_Benefit_Related_To_Share_Based_Compensation_Expense_2023", parseFieldForYear(rightPage, "Income tax benefit related to share-based compensation expense"));
            data.put("Share_Based_Compensation", shareBasedCompensation);

            Map<String, Object> purchaseObligations = new HashMap<>();
            purchaseObligations.put("2024_Obligation", parseFieldForYear(rightPage, "2024"));
            purchaseObligations.put("2025_Obligation", parseFieldForYear(rightPage, "2025"));
            purchaseObligations.put("2026_Obligation", parseFieldForYear(rightPage, "2026"));
            purchaseObligations.put("2027_Obligation", parseFieldForYear(rightPage, "2027"));
            purchaseObligations.put("2028_Obligation", parseFieldForYear(rightPage, "2028"));
            purchaseObligations.put("Thereafter_Obligation", parseFieldForYear(rightPage, "Thereafter"));
            purchaseObligations.put("Total_Obligation", parseFieldForYear(rightPage, "Total"));
            data.put("Unconditional_Purchase_Obligations", purchaseObligations);
        }
        return data;
    }

    //20
    private Map<String, Object> extractSegmentInformationAndGeographicData(String[] pages) {
        Map<String, Object> data = new HashMap<>();

        String rightPage = findPageWithText(pages, "Note 13 – Segment Information and Geographic Data");

        if (rightPage != null) {

            Map<String, Object> americas = new HashMap<>();
            americas.put("Net_Sales", parseSegmentData(rightPage, "Americas", "Net sales"));
            americas.put("Operating_Income", parseSegmentData(rightPage, "Americas", "Operating income"));
            data.put("Americas", americas);

            Map<String, Object> europe = new HashMap<>();
            europe.put("Net_Sales", parseSegmentData(rightPage, "Europe", "Net sales"));
            europe.put("Operating_Income", parseSegmentData(rightPage, "Europe", "Operating income"));
            data.put("Europe", europe);

            Map<String, Object> greaterChina = new HashMap<>();
            greaterChina.put("Net_Sales", parseSegmentData(rightPage, "Greater China", "Net sales"));
            greaterChina.put("Operating_Income", parseSegmentData(rightPage, "Greater China", "Operating income"));
            data.put("Greater_China", greaterChina);

            Map<String, Object> japan = new HashMap<>();
            japan.put("Net_Sales", parseSegmentData(rightPage, "Japan", "Net sales"));
            japan.put("Operating_Income", parseSegmentData(rightPage, "Japan", "Operating income"));
            data.put("Japan", japan);

            Map<String, Object> restOfAsiaPacific = new HashMap<>();
            restOfAsiaPacific.put("Net_Sales", parseSegmentData(rightPage, "Rest of Asia Pacific", "Net sales"));
            restOfAsiaPacific.put("Operating_Income", parseSegmentData(rightPage, "Rest of Asia Pacific", "Operating income"));
            data.put("Rest_of_Asia_Pacific", restOfAsiaPacific);

            Map<String, Object> reconciliation = new HashMap<>();
            reconciliation.put("Segment_Operating_Income", parseFieldForYear(rightPage, "Segment operating income"));
            reconciliation.put("Research_and_Development_Expense", parseFieldForYear(rightPage, "Research and development expense"));
            reconciliation.put("Other_Corporate_Expenses_Net", parseFieldForYear(rightPage, "Other corporate expenses, net (1)  "));
            reconciliation.put("Total_Operating_Income", parseFieldForYear(rightPage, "Total operating income"));
            data.put("Reconciliation", reconciliation);
        }

        return data;
    }

    //21
    private Map<String, Object> extractNetSalesAndLongLivedAssets(String[] pages) {
        Map<String, Object> data = new HashMap<>();

        String rightPage = findPageWithText(pages, "The U.S. and China were the only countries that accounted for more than 10%");

        if (rightPage != null) {

            Map<String, Object> netSales = new HashMap<>();
            netSales.put("U.S.", parseFieldForYear(rightPage, "U.S."));
            netSales.put("China", parseFieldForYear(rightPage, "China"));
            netSales.put("Other_Countries", parseFieldForYear(rightPage, "Other countries"));
            netSales.put("Total_Net_Sales", parseFieldForYear(rightPage, "Total net sales"));
            data.put("Net_Sales", netSales);

            Map<String, Object> longLivedAssets = new HashMap<>();
            longLivedAssets.put("U.S.", parseFieldForYear(rightPage, "U.S."));
            longLivedAssets.put("China", parseFieldForYear(rightPage, "China"));
            longLivedAssets.put("Other_Countries", parseFieldForYear(rightPage, "Other countries"));
            longLivedAssets.put("Total_Long_Lived_Assets", parseFieldForYear(rightPage, "Total long-lived assets"));
            data.put("Long_Lived_Assets", longLivedAssets);
        }

        return data;
    }
}