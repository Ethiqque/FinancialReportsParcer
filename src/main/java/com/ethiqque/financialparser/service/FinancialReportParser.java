package com.ethiqque.financialparser.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service class for parsing financial report PDFs.
 * It extracts relevant financial data such as assets, liabilities, income statements,
 * and other key metrics from the provided PDF, all data is collected from 2023.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialReportParser {

    private Executor asyncExecutor;

    /**
     * Parses the input PDF file and extracts relevant financial data.
     *
     * @param inputStream the InputStream of the PDF to be parsed
     * @return a Map containing extracted financial data
     * @throws IOException if there is an issue reading or processing the PDF
     */
    public Map<String, Object> parsePdf(InputStream inputStream) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try (PDDocument document = PDDocument.load(inputStream)) {
            log.info("Loaded PDF document");

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Extracted PDF Text");

            String[] pages = text.split("Apple Inc. | 2023 Form 10-K");

            data = extractFinancialData(pages);
        } catch (IOException e) {
            log.error("Error processing PDF: {}", e.getMessage());
            throw e;
        }

        return data;
    }

    /**
     * Extracts the financial data from the parsed pages of the report.
     * Combines various asynchronous operations to gather all necessary financial details.
     *
     * @param pages an array of strings representing the pages of the PDF document
     * @return a Map containing the extracted financial data
     */
    private Map<String, Object> extractFinancialData(String[] pages) {
        Map<String, Object> data = new HashMap<>();

        CompletableFuture<Map<String, Object>> assetsFuture = extractAssets(pages);
        CompletableFuture<Map<String, Object>> liabilitiesFuture = extractLiabilitiesAndEquity(pages);
        CompletableFuture<Map<String, Object>> incomeStatementFuture = extractIncomeStatementAndEPS(pages);
        CompletableFuture<Map<String, Object>> comprehensiveIncomeFuture = extractComprehensiveIncomeStatement(pages);
        CompletableFuture<Map<String, Object>> shareholdersEquityFuture = extractShareholdersEquityStatement(pages);
        CompletableFuture<Map<String, Object>> cashFlowStatementFuture = extractCashFlowStatement(pages);
        CompletableFuture<Map<String, Object>> netSalesAndEPSFuture = extractNetSalesAndEPS(pages);
        CompletableFuture<Map<String, Object>> cashEquivalentsFuture = extractCashEquivalentsAndMarketableSecurities(pages);
        CompletableFuture<Map<String, Object>> debtSecuritiesFuture = extractDebtSecuritiesAndDerivatives(pages);
        CompletableFuture<Map<String, Object>> hedgedAssetsFuture = extractHedgedAssetsAndLiabilities(pages);
        CompletableFuture<Map<String, Object>> propertyPlantAndEquipmentFuture = extractPropertyPlantAndEquipmentAndOtherDetails(pages);
        CompletableFuture<Map<String, Object>> incomeTaxesFuture = extractIncomeTaxes(pages);
        CompletableFuture<Map<String, Object>> deferredTaxFuture = extractDeferredTaxAndUncertainPositions(pages);
        CompletableFuture<Map<String, Object>> commercialPaperFuture = extractCommercialPaper(pages);
        CompletableFuture<Map<String, Object>> leaseLiabilitiesFuture = extractLeaseLiabilityMaturities(pages);
        CompletableFuture<Map<String, Object>> termDebtFuture = extractTermDebt(pages);
        CompletableFuture<Map<String, Object>> commonStockFuture = extractCommonStock(pages);
        CompletableFuture<Map<String, Object>> shareBasedCompensationFuture = extractShareBasedCompensationAndPurchaseObligations(pages);
        CompletableFuture<Map<String, Object>> segmentInformationFuture = extractSegmentInformationAndGeographicData(pages);
        CompletableFuture<Map<String, Object>> netSalesLongLivedAssetsFuture = extractNetSalesAndLongLivedAssets(pages);

        CompletableFuture.allOf(
                assetsFuture, liabilitiesFuture, incomeStatementFuture, comprehensiveIncomeFuture,
                shareholdersEquityFuture, cashFlowStatementFuture, netSalesAndEPSFuture, cashEquivalentsFuture,
                debtSecuritiesFuture, hedgedAssetsFuture, propertyPlantAndEquipmentFuture, incomeTaxesFuture,
                deferredTaxFuture, commercialPaperFuture, leaseLiabilitiesFuture, termDebtFuture,
                commonStockFuture, shareBasedCompensationFuture, segmentInformationFuture, netSalesLongLivedAssetsFuture
        ).join();

        try {
            data.put("Assets", assetsFuture.get());
            data.put("Liabilities_and_Shareholders_Equity", liabilitiesFuture.get());
            data.put("Income_Statement_And_EPS", incomeStatementFuture.get());
            data.put("Comprehensive_Income_Statement", comprehensiveIncomeFuture.get());
            data.put("Shareholders_Equity_Statement", shareholdersEquityFuture.get());
            data.put("Cash_Flow_Statement", cashFlowStatementFuture.get());
            data.put("Net_Sales_And_EPS", netSalesAndEPSFuture.get());
            data.put("Cash_Equivalents_And_Marketable_Securities", cashEquivalentsFuture.get());
            data.put("Debt_Securities_And_Derivatives", debtSecuritiesFuture.get());
            data.put("Hedged_Assets_And_Liabilities", hedgedAssetsFuture.get());
            data.put("Property_Plant_And_Equipment_And_Other_Details", propertyPlantAndEquipmentFuture.get());
            data.put("Income_Taxes", incomeTaxesFuture.get());
            data.put("Deferred_Tax_And_Uncertain_Positions", deferredTaxFuture.get());
            data.put("Commercial_Paper", commercialPaperFuture.get());
            data.put("Lease_Liability_Maturities", leaseLiabilitiesFuture.get());
            data.put("Term_Debt", termDebtFuture.get());
            data.put("Common_Stock", commonStockFuture.get());
            data.put("Share_Based_Compensation_And_Purchase_Obligations", shareBasedCompensationFuture.get());
            data.put("Segment_Information_And_Geographic_Data", segmentInformationFuture.get());
            data.put("Net_Sales_And_Long_Lived_Assets", netSalesLongLivedAssetsFuture.get());
        } catch (Exception e) {
            log.error("Error gathering data: {}", e.getMessage());
        }

        return data;
    }

    /**
     * Searches through the text of the PDF pages to find the page that contains the specified keyword.
     *
     * @param pages the array of PDF pages represented as strings
     * @param keyword the keyword to search for
     * @return the page containing the keyword, or null if no such page is found
     */
    private String findPageWithText(String[] pages, String keyword) {
        for (String page : pages) {
            if (page.contains(keyword)) {
                return page;
            }
        }
        return null;
    }

    /**
     * Parses a specific field in the financial data and returns its value for the corresponding year.
     *
     * @param text the text of the PDF page
     * @param fieldName the name of the field to be extracted
     * @return the parsed numeric value for the field, or 0.0 if not found
     */
    private double parseFieldForYear(String text, String fieldName) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().contains(fieldName)) {
                log.info("Field found: {}", fieldName);

                String cleanedLine = line.replaceAll("\\(\\d+\\)", "").trim();

                String[] tokens = cleanedLine.split("\\s+");
                for (String token : tokens) {
                    if (token.matches("\\(.*?\\)") || token.matches("-?\\d+(,\\d{3})*(\\.\\d+)?")) {
                        return parseNumericString(token);
                    }
                }
            }
        }
        return 0.0;
    }

    /**
     * A general field parser that extracts numeric values based on field names.
     *
     * @param text the text of the PDF
     * @param fieldName the name of the field to look for
     * @return the numeric value of the field, or 0.0 if not found
     */
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

    /**
     * Parses a numeric string and converts it to a double value.
     * Handles negative values enclosed in parentheses.
     *
     * @param numericString the string to be parsed
     * @return the double value of the numeric string
     */
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

    /**
     * Extracts the financial data for a specific segment and label.
     *
     * @param text the text of the PDF
     * @param segment the segment to look for
     * @param label the label within the segment
     * @return the numeric value of the segment data, or 0.0 if not found
     */
    private double parseSegmentData(String text, String segment, String label) {
        String pattern = segment + ":[\\s\\S]*?" + label + "\\s*\\$\\s*([0-9,]+)";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(text);
        if (matcher.find()) {
            return parseNumericString(matcher.group(1));
        }
        return 0.0;
    }

    /**
     * Asynchronously extracts the income statement and EPS data from the PDF.
     *
     * @param pages the pages of the PDF
     * @return a CompletableFuture containing the extracted income statement and EPS data
     */
    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractIncomeStatementAndEPS(String[] pages) {
        Map<String, Object> data = new HashMap<>();
        String rightPage = findPageWithText(pages, "CONSOLIDATED STATEMENTS OF OPERATIONS");
        if (rightPage != null) {
            data.put("Income_Statement", extractIncomeStatementData(rightPage));
            data.put("Earnings_Per_Share", extractEPSData(rightPage));
        }
        return CompletableFuture.completedFuture(data);
    }

    private Map<String, Object> extractIncomeStatementData(String rightPage) {
        Map<String, Object> incomeStatement = new HashMap<>();
        incomeStatement.put("Products_Net_Sales", parseFieldForYear(rightPage, "Products"));
        incomeStatement.put("Services_Net_Sales", parseFieldForYear(rightPage, "Services"));
        incomeStatement.put("Total_Net_Sales", parseFieldForYear(rightPage, "Total net sales"));
        incomeStatement.put("Total_Cost_of_Sales", parseFieldForYear(rightPage, "Total cost of sales"));
        incomeStatement.put("Gross_Margin", parseFieldForYear(rightPage, "Gross margin"));
        incomeStatement.put("Operating_Income", parseFieldForYear(rightPage, "Operating income"));
        return incomeStatement;
    }

    private Map<String, Object> extractEPSData(String rightPage) {
        Map<String, Object> earningsPerShare = new HashMap<>();
        earningsPerShare.put("Basic_EPS", parseFieldForYear(rightPage, "Basic"));
        earningsPerShare.put("Diluted_EPS", parseFieldForYear(rightPage, "Diluted"));
        return earningsPerShare;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractAssets(String[] pages) {
        Map<String, Object> assets = new HashMap<>();
        String rightPage = findPageWithText(pages, "CONSOLIDATED BALANCE SHEETS");
        if (rightPage != null) {
            assets.put("Current_Assets", extractCurrentAssets(rightPage));
            assets.put("Non_Current_Assets", extractNonCurrentAssets(rightPage));
        }
        return CompletableFuture.completedFuture(assets);
    }

    private Map<String, Object> extractCurrentAssets(String rightPage) {
        Map<String, Object> currentAssets = new HashMap<>();
        currentAssets.put("Cash_and_Cash_Equivalents", parseFieldForYear(rightPage, "Cash and cash equivalents"));
        currentAssets.put("Marketable_Securities", parseFieldForYear(rightPage, "Marketable securities"));
        return currentAssets;
    }

    private Map<String, Object> extractNonCurrentAssets(String rightPage) {
        Map<String, Object> nonCurrentAssets = new HashMap<>();
        nonCurrentAssets.put("Marketable_Securities", parseFieldForYear(rightPage, "Marketable securities"));
        nonCurrentAssets.put("Property_Plant_and_Equipment_Net", parseFieldForYear(rightPage, "Property, plant and equipment, net"));
        return nonCurrentAssets;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractLiabilitiesAndEquity(String[] pages) {
        Map<String, Object> liabilitiesAndEquity = new HashMap<>();
        String rightPage = findPageWithText(pages, "LIABILITIES AND SHAREHOLDERS’ EQUITY:");
        if (rightPage != null) {
            liabilitiesAndEquity.put("Current_Liabilities", extractCurrentLiabilities(rightPage));
            liabilitiesAndEquity.put("Non_Current_Liabilities", extractNonCurrentLiabilities(rightPage));
        }
        return CompletableFuture.completedFuture(liabilitiesAndEquity);
    }

    private Map<String, Object> extractCurrentLiabilities(String rightPage) {
        Map<String, Object> currentLiabilities = new HashMap<>();
        currentLiabilities.put("Accounts_Payable", parseFieldForYear(rightPage, "Accounts payable"));
        currentLiabilities.put("Other_Current_Liabilities", parseFieldForYear(rightPage, "Other current liabilities"));
        return currentLiabilities;
    }

    private Map<String, Object> extractNonCurrentLiabilities(String rightPage) {
        Map<String, Object> nonCurrentLiabilities = new HashMap<>();
        nonCurrentLiabilities.put("Term_Debt", parseFieldForYear(rightPage, "Term debt"));
        nonCurrentLiabilities.put("Other_Non_Current_Liabilities", parseFieldForYear(rightPage, "Other non-current liabilities"));
        return nonCurrentLiabilities;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractComprehensiveIncomeStatement(String[] pages) {
        Map<String, Object> comprehensiveIncome = new HashMap<>();
        String rightPage = findPageWithText(pages, "CONSOLIDATED STATEMENTS OF COMPREHENSIVE INCOME");
        if (rightPage != null) {
            comprehensiveIncome.put("Net_Income", parseFieldForYear(rightPage, "Net income"));
            comprehensiveIncome.put("Change_in_Foreign_Currency_Translation", parseFieldForYear(rightPage, "Change in foreign currency translation"));
            comprehensiveIncome.put("Change_in_Fair_Value_of_Derivative_Instruments", parseFieldForYear(rightPage, "Change in fair value of derivative instruments"));
            comprehensiveIncome.put("Total_Other_Comprehensive_Income_Loss", parseFieldForYear(rightPage, "Total other comprehensive income/(loss)"));
        }
        return CompletableFuture.completedFuture(comprehensiveIncome);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractShareholdersEquityStatement(String[] pages) {
        Map<String, Object> shareholdersEquity = new HashMap<>();
        String rightPage = findPageWithText(pages, "CONSOLIDATED STATEMENTS OF SHAREHOLDERS’ EQUITY");
        if (rightPage != null) {
            shareholdersEquity.put("Beginning_Balances_Total_Shareholders_Equity", parseFieldForYear(rightPage, "Total shareholders’ equity, beginning balances"));
            shareholdersEquity.put("Net_Income", parseFieldForYear(rightPage, "Net income"));
            shareholdersEquity.put("Dividends_Declared", parseFieldForYear(rightPage, "Dividends and dividend equivalents declared"));
            shareholdersEquity.put("Ending_Balances_Total_Shareholders_Equity", parseFieldForYear(rightPage, "Total shareholders’ equity, ending balances"));
        }
        return CompletableFuture.completedFuture(shareholdersEquity);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractCashFlowStatement(String[] pages) {
        Map<String, Object> cashFlowStatement = new HashMap<>();
        String rightPage = findPageWithText(pages, "CONSOLIDATED STATEMENTS OF CASH FLOWS");
        if (rightPage != null) {
            cashFlowStatement.put("Net_Income", parseFieldForYear(rightPage, "Net income"));
            cashFlowStatement.put("Depreciation_and_Amortization", parseFieldForYear(rightPage, "Depreciation and amortization"));
            cashFlowStatement.put("Cash_Generated_by_Operating_Activities", parseFieldForYear(rightPage, "Cash generated by operating activities"));
            cashFlowStatement.put("Cash_Used_in_Investing_Activities", parseFieldForYear(rightPage, "Cash used in investing activities"));
            cashFlowStatement.put("Cash_Used_in_Financing_Activities", parseFieldForYear(rightPage, "Cash used in financing activities"));
        }
        return CompletableFuture.completedFuture(cashFlowStatement);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractNetSalesAndEPS(String[] pages) {
        Map<String, Object> netSalesAndEPS = new HashMap<>();
        String rightPage = findPageWithText(pages, "Note 3 – Earnings Per Share");
        if (rightPage != null) {
            Map<String, Object> netSales = new HashMap<>();

            netSales.put("iPhone", parseFieldForYear(rightPage, "iPhone (1) $"));
            netSales.put("Mac", parseFieldForYear(rightPage, "Mac (1)"));
            netSales.put("iPad", parseFieldForYear(rightPage, "iPad (1)"));
            netSales.put("Wearables_Home_and_Accessories", parseFieldForYear(rightPage, "Wearables, Home and Accessories (1)"));
            netSales.put("Services", parseFieldForYear(rightPage, "Services (2)"));
            netSales.put("Total_Net_Sales", parseFieldForYear(rightPage, "Total net sales $"));
            netSalesAndEPS.put("Net_Sales", netSales);

            Map<String, Object> eps = new HashMap<>();
            eps.put("Basic_Earnings_Per_Share", parseFieldForYear(rightPage, "Basic earnings per share"));
            eps.put("Diluted_Earnings_Per_Share", parseFieldForYear(rightPage, "Diluted earnings per share"));
            netSalesAndEPS.put("Earnings_Per_Share", eps);
        }
        return CompletableFuture.completedFuture(netSalesAndEPS);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractCashEquivalentsAndMarketableSecurities(String[] pages) {
        Map<String, Object> cashEquivalentsAndMarketableSecurities = new HashMap<>();
        String rightPage = findPageWithText(pages, "Cash, Cash Equivalents and Marketable Securities");
        if (rightPage != null) {
            Map<String, Object> level1 = new HashMap<>();
            level1.put("Money_Market_Funds", parseFieldForYear(rightPage, "Money market funds"));
            level1.put("Mutual_Funds_Equity_Securities", parseFieldForYear(rightPage, "Mutual funds and equity securities"));
            cashEquivalentsAndMarketableSecurities.put("Level_1", level1);

            Map<String, Object> level2 = new HashMap<>();
            level2.put("U.S_Treasury_Securities", parseFieldForYear(rightPage, "U.S. Treasury securities"));
            level2.put("Corporate_Debt_Securities", parseFieldForYear(rightPage, "Corporate debt securities"));
            cashEquivalentsAndMarketableSecurities.put("Level_2", level2);
        }
        return CompletableFuture.completedFuture(cashEquivalentsAndMarketableSecurities);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractDebtSecuritiesAndDerivatives(String[] pages) {
        Map<String, Object> debtSecuritiesAndDerivatives = new HashMap<>();
        String rightPage = findPageWithText(pages, "Derivative Instruments and Hedging");
        if (rightPage != null) {
            debtSecuritiesAndDerivatives.put("Non_Current_Marketable_Debt_Securities", extractNonCurrentDebtSecurities(rightPage));
            debtSecuritiesAndDerivatives.put("Derivative_Instruments", extractDerivativeInstruments(rightPage));
        }
        return CompletableFuture.completedFuture(debtSecuritiesAndDerivatives);
    }

    private Map<String, Object> extractNonCurrentDebtSecurities(String rightPage) {
        Map<String, Object> nonCurrentDebtSecurities = new HashMap<>();
        nonCurrentDebtSecurities.put("Due_After_1_Year_Through_5_Years", parseField(rightPage, "Due after 1 year through 5 years"));
        nonCurrentDebtSecurities.put("Due_After_10_Years", parseField(rightPage, "Due after 10 years"));
        nonCurrentDebtSecurities.put("Total_Fair_Value", parseField(rightPage, "Total fair value"));
        return nonCurrentDebtSecurities;
    }

    private Map<String, Object> extractDerivativeInstruments(String rightPage) {
        Map<String, Object> derivativeInstruments = new HashMap<>();
        derivativeInstruments.put("Accounting_Hedges", extractAccountingHedges(rightPage));
        derivativeInstruments.put("Non_Accounting_Hedges", extractNonAccountingHedges(rightPage));
        return derivativeInstruments;
    }

    private Map<String, Object> extractAccountingHedges(String rightPage) {
        Map<String, Object> accountingHedges = new HashMap<>();
        accountingHedges.put("Foreign_Exchange_Contracts", parseField(rightPage, "Foreign exchange contracts"));
        accountingHedges.put("Interest_Rate_Contracts", parseField(rightPage, "Interest rate contracts"));
        return accountingHedges;
    }

    private Map<String, Object> extractNonAccountingHedges(String rightPage) {
        Map<String, Object> nonAccountingHedges = new HashMap<>();
        nonAccountingHedges.put("Foreign_Exchange_Contracts", parseField(rightPage, "Foreign exchange contracts"));
        return nonAccountingHedges;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractHedgedAssetsAndLiabilities(String[] pages) {
        Map<String, Object> hedgedAssetsAndLiabilities = new HashMap<>();
        String rightPage = findPageWithText(pages, "Accounts Receivable");
        if (rightPage != null) {
            hedgedAssetsAndLiabilities.put("Marketable_Securities", parseFieldForYear(rightPage, "Current and non-current marketable securities"));
            hedgedAssetsAndLiabilities.put("Term_Debt", parseFieldForYear(rightPage, "Current and non-current term debt"));
        }
        return CompletableFuture.completedFuture(hedgedAssetsAndLiabilities);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractPropertyPlantAndEquipmentAndOtherDetails(String[] pages) {
        Map<String, Object> propertyAndEquipmentDetails = new HashMap<>();
        String rightPage = findPageWithText(pages, "Note 5 – Property, Plant and Equipment");
        if (rightPage != null) {
            propertyAndEquipmentDetails.put("Net_Property_Plant_And_Equipment", parseFieldForYear(rightPage, "Total property, plant and equipment, net"));
        }
        return CompletableFuture.completedFuture(propertyAndEquipmentDetails);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractIncomeTaxes(String[] pages) {
        Map<String, Object> incomeTaxes = new HashMap<>();
        String rightPage = findPageWithText(pages, "Note 7 – Income Taxes");
        if (rightPage != null) {
            incomeTaxes.put("Provision_For_Income_Taxes", parseFieldForYear(rightPage, "Provision for income taxes"));
        }
        return CompletableFuture.completedFuture(incomeTaxes);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractDeferredTaxAndUncertainPositions(String[] pages) {
        Map<String, Object> deferredTaxData = new HashMap<>();
        String rightPage = findPageWithText(pages, "Deferred Tax Assets and Liabilities");
        if (rightPage != null) {
            deferredTaxData.put("Deferred_Tax_Assets", extractDeferredTaxAssets(rightPage));
            deferredTaxData.put("Deferred_Tax_Liabilities", extractDeferredTaxLiabilities(rightPage));
        }
        return CompletableFuture.completedFuture(deferredTaxData);
    }

    private Map<String, Object> extractDeferredTaxAssets(String rightPage) {
        Map<String, Object> deferredTaxAssets = new HashMap<>();
        deferredTaxAssets.put("Total_Deferred_Tax_Assets", parseFieldForYear(rightPage, "Total deferred tax assets"));
        return deferredTaxAssets;
    }

    private Map<String, Object> extractDeferredTaxLiabilities(String rightPage) {
        Map<String, Object> deferredTaxLiabilities = new HashMap<>();
        deferredTaxLiabilities.put("Total_Deferred_Tax_Liabilities", parseFieldForYear(rightPage, "Total deferred tax liabilities"));
        return deferredTaxLiabilities;
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractCommercialPaper(String[] pages) {
        Map<String, Object> commercialPaper = new HashMap<>();
        String rightPage = findPageWithText(pages, "Note 9 – Debt");
        if (rightPage != null) {
            commercialPaper.put("Proceeds_Repayments_Net", parseFieldForYear(rightPage, "Proceeds from/(Repayments of) commercial paper, net"));
        }
        return CompletableFuture.completedFuture(commercialPaper);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractLeaseLiabilityMaturities(String[] pages) {
        Map<String, Object> leaseMaturities = new HashMap<>();
        String rightPage = findPageWithText(pages, "Note 9 – Debt");
        if (rightPage != null) {
            leaseMaturities.put("Operating_Leases", parseFieldForYear(rightPage, "Total lease liabilities"));
        }
        return CompletableFuture.completedFuture(leaseMaturities);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractTermDebt(String[] pages) {
        Map<String, Object> termDebtData = new HashMap<>();
        String rightPage = findPageWithText(pages, "Note 10 – Shareholders’ Equity");
        if (rightPage != null) {
            termDebtData.put("Total_Term_Debt_Principal", parseFieldForYear(rightPage, "Total term debt principal"));
        }
        return CompletableFuture.completedFuture(termDebtData);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractCommonStock(String[] pages) {
        Map<String, Object> commonStockData = new HashMap<>();
        String rightPage = findPageWithText(pages, "Note 11 – Share-Based Compensation");
        if (rightPage != null) {
            commonStockData.put("Common_Stock_Beginning_Balance", parseFieldForYear(rightPage, "Common stock outstanding, beginning balances"));
            commonStockData.put("Common_Stock_Ending_Balance", parseFieldForYear(rightPage, "Common stock outstanding, ending balances"));
        }
        return CompletableFuture.completedFuture(commonStockData);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractShareBasedCompensationAndPurchaseObligations(String[] pages) {
        Map<String, Object> data = new HashMap<>();
        String rightPage = findPageWithText(pages, "Note 12 – Commitments, Contingencies and Supply Concentrations");
        if (rightPage != null) {
            data.put("Share_Based_Compensation_Expense", parseFieldForYear(rightPage, "Share-based compensation expense"));
        }
        return CompletableFuture.completedFuture(data);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractSegmentInformationAndGeographicData(String[] pages) {
        Map<String, Object> data = new HashMap<>();
        String rightPage = findPageWithText(pages, "Note 13 – Segment Information and Geographic Data");
        if (rightPage != null) {
            data.put("Americas", parseSegmentData(rightPage, "Americas", "Net sales"));
            data.put("Europe", parseSegmentData(rightPage, "Europe", "Net sales"));
        }
        return CompletableFuture.completedFuture(data);
    }

    @Async("asyncExecutor")
    public CompletableFuture<Map<String, Object>> extractNetSalesAndLongLivedAssets(String[] pages) {
        Map<String, Object> data = new HashMap<>();
        String rightPage = findPageWithText(pages, "The U.S. and China were the only countries that accounted for more than 10%");
        if (rightPage != null) {
            data.put("Net_Sales", parseFieldForYear(rightPage, "Total net sales"));
        }
        return CompletableFuture.completedFuture(data);
    }
}
