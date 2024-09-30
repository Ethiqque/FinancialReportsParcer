# PDF Financial Report Parser

This service parses the **United States Securities and Exchange Commission (SEC) Form 10-K** financial report for **Apple Inc.**. It extracts various financial data from the 2023 report and returns the results in JSON format.

### Report Link:
[Download Apple Financial Report - Form 10-K (2023)](https://s2.q4cdn.com/470004039/files/doc_earnings/2023/q4/filing/_10-K-Q4-2023-As-Filed.pdf)

### Data Extracted:
The parser extracts detailed financial statistics from the 2023 financial report. Here is an example of the returned JSON structure:

```json
{
    "Share_Based_Compensation_And_Purchase_Obligations": {
        "Share_Based_Compensation_Expense": 10833.0
    },
    "Comprehensive_Income_Statement": {
        "Total_Other_Comprehensive_Income_Loss": -11272.0,
        "Change_in_Foreign_Currency_Translation": -1511.0,
        "Change_in_Fair_Value_of_Derivative_Instruments": 323.0,
        "Net_Income": 96995.0
    },
    "Commercial_Paper": {
        "Proceeds_Repayments_Net": -1333.0
    },
    "Net_Sales_And_EPS": {
        "Earnings_Per_Share": {
            "Diluted_Earnings_Per_Share": 6.13,
            "Basic_Earnings_Per_Share": 6.16
        },
        "Net_Sales": {
            "iPhone": 200583.0,
            "Services": 85200.0,
            "Wearables_Home_and_Accessories": 39845.0,
            "iPad": 28300.0,
            "Total_Net_Sales": 383285.0,
            "Mac": 29357.0
        }
    },
    "Common_Stock": {
        "Common_Stock_Ending_Balance": 1.5550061E7,
        "Common_Stock_Beginning_Balance": 1.5943425E7
    },
    "Liabilities_and_Shareholders_Equity": {
        "Current_Liabilities": {
            "Other_Current_Liabilities": 58829.0,
            "Accounts_Payable": 62611.0
        },
        "Non_Current_Liabilities": {
            "Other_Non_Current_Liabilities": 49848.0,
            "Term_Debt": 9822.0
        }
    },
    "Net_Sales_And_Long_Lived_Assets": {
        "Net_Sales": 383285.0
    },
    "Property_Plant_And_Equipment_And_Other_Details": {
        "Net_Property_Plant_And_Equipment": 43715.0
    },
    "Term_Debt": {
        "Total_Term_Debt_Principal": 106572.0
    },
    "Debt_Securities_And_Derivatives": {
        "Derivative_Instruments": {
            "Non_Accounting_Hedges": {
                "Foreign_Exchange_Contracts": 74730.0
            },
            "Accounting_Hedges": {
                "Foreign_Exchange_Contracts": 74730.0,
                "Interest_Rate_Contracts": 19375.0
            }
        },
        "Non_Current_Marketable_Debt_Securities": {
            "Due_After_10_Years": 16153.0,
            "Total_Fair_Value": 100544.0,
            "Due_After_1_Year_Through_5_Years": 74427.0
        }
    },
    "Cash_Flow_Statement": {
        "Cash_Used_in_Financing_Activities": -108488.0,
        "Cash_Used_in_Investing_Activities": 0.0,
        "Depreciation_and_Amortization": 11519.0,
        "Net_Income": 96995.0,
        "Cash_Generated_by_Operating_Activities": 110543.0
    },
    "Deferred_Tax_And_Uncertain_Positions": {
        "Deferred_Tax_Liabilities": {
            "Total_Deferred_Tax_Liabilities": 7118.0
        },
        "Deferred_Tax_Assets": {
            "Total_Deferred_Tax_Assets": 32743.0
        }
    },
    "Cash_Equivalents_And_Marketable_Securities": {
        "Level_2": {
            "U.S_Treasury_Securities": 19406.0,
            "Corporate_Debt_Securities": 76840.0
        },
        "Level_1": {
            "Money_Market_Funds": 481.0,
            "Mutual_Funds_Equity_Securities": 442.0
        }
    },
    "Shareholders_Equity_Statement": {
        "Ending_Balances_Total_Shareholders_Equity": 62146.0,
        "Beginning_Balances_Total_Shareholders_Equity": 50672.0,
        "Dividends_Declared": -14996.0,
        "Net_Income": 96995.0
    },
    "Assets": {
        "Non_Current_Assets": {
            "Marketable_Securities": 31590.0,
            "Property_Plant_and_Equipment_Net": 43715.0
        },
        "Current_Assets": {
            "Marketable_Securities": 31590.0,
            "Cash_and_Cash_Equivalents": 29965.0
        }
    },
    "Lease_Liability_Maturities": {
        "Operating_Leases": 11818.0
    },
    "Income_Taxes": {
        "Provision_For_Income_Taxes": 16741.0
    },
    "Segment_Information_And_Geographic_Data": {
        "Americas": 162560.0,
        "Europe": 94294.0
    },
    "Income_Statement_And_EPS": {
        "Earnings_Per_Share": {
            "Basic_EPS": 6.16,
            "Diluted_EPS": 6.13
        },
        "Income_Statement": {
            "Total_Cost_of_Sales": 214137.0,
            "Gross_Margin": 169148.0,
            "Services_Net_Sales": 85200.0,
            "Products_Net_Sales": 298085.0,
            "Total_Net_Sales": 383285.0,
            "Operating_Income": 114301.0
        }
    },
    "Hedged_Assets_And_Liabilities": {
        "Marketable_Securities": 14433.0,
        "Term_Debt": -18247.0
    }
}
```

## How to Use?

### 1. Run the Backend Application
Start the backend service on your local machine. Ensure the application is running and accessible.

![Running Application Screenshot](https://github.com/user-attachments/assets/c72ae228-70c1-4599-8dff-b23cea1adcfb)

### 2. Open Postman
Launch **Postman** to make a request to the backend service.

### 3. Create a POST Request in Postman

- Set the request type to `POST`.
- Use the following URL:
  ```
  http://localhost:8080/api/financial-report/upload
  ```

### 4. Configure the Request Body

- Click on the `Body` tab in Postman.
- Choose `form-data`.
- In the `Key` field, type `file`.
- Ensure the value type is set to `File` (not `Text`).

![Postman File Upload Example](https://github.com/user-attachments/assets/698694f0-99c0-46a6-a179-b8c5b8a6914a)

### 5. Upload the Apple Financial Report
Select the downloaded **Apple Financial Report (2023 Form 10-K)** PDF from your computer.

### 6. Send the Request
Click **Send** in Postman. You should receive a JSON response containing the parsed financial data from the PDF.

You're all set!
