package com.wallet.app.controller;

import com.wallet.app.model.TransactionModel;
import com.wallet.app.model.TransactionModel.Status;
import com.wallet.app.service.MockTransactionService;
import com.wallet.app.service.TransactionService;
import com.wallet.app.util.CurrencyFormatter;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryController {
    @FXML
    private TextField searchField;
    
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private ListView<TransactionModel> transactionList;
    
    @FXML
    private Button filterAll;
    @FXML
    private Button filterSent;
    @FXML
    private Button filterReceived;
    @FXML
    private Button filterPending;
    
    private TransactionService transactionService;
    private List<TransactionModel> allTransactions;
    private String currentFilter = "ALL";
    
    @FXML
    public void initialize() {
        transactionService = new MockTransactionService();
        allTransactions = transactionService.getTransactions();
        
        // Setup custom cell factory for transactions
        setupCellFactory();
        
        loadTransactions();
        
        // Add listeners for search and date picker
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTransactions();
            updateClearButtonState();
        });
        
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterTransactions();
            updateClearButtonState();
        });
        
        // Initialize clear button state
        updateClearButtonState();
        
        // Add click listener for transaction details
        transactionList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TransactionModel selectedItem = transactionList.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getTxId() != null) {
                    showTransactionDetails(selectedItem);
                }
            }
        });
    }
    
    private void setupCellFactory() {
        transactionList.setCellFactory(listView -> new ListCell<TransactionModel>() {
            @Override
            protected void updateItem(TransactionModel tx, boolean empty) {
                super.updateItem(tx, empty);
                
                if (empty || tx == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("transaction-cell");
                } else {
                    // Check if this is a day separator or empty message
                    if (tx.getTxId() == null) {
                        if (tx.getDate() == null) {
                            // Empty message
                            Label message = new Label("No transactions found");
                            message.getStyleClass().add("separator");
                            message.setMaxWidth(Double.MAX_VALUE);
                            message.setAlignment(Pos.CENTER);
                            setGraphic(message);
                        } else {
                            // Day separator - use the address field which stores the label
                            Label separator = new Label(tx.getAddress());
                            separator.getStyleClass().add("separator");
                            separator.setMaxWidth(Double.MAX_VALUE);
                            separator.setAlignment(Pos.CENTER_LEFT);
                            separator.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12 0 8 0;");
                            setGraphic(separator);
                        }
                        getStyleClass().removeAll("transaction-cell");
                    } else {
                        // Transaction item
                        VBox container = new VBox(6);
                        container.setAlignment(Pos.CENTER_LEFT);
                        
                        // Top row: Status icon, Status, Amount
                        HBox topRow = new HBox(10);
                        topRow.setAlignment(Pos.CENTER_LEFT);
                        
                        // Status icon
                        Label statusIcon = new Label(getStatusIcon(tx.getStatus()));
                        statusIcon.getStyleClass().add("status-icon");
                        statusIcon.setMinWidth(25);
                        statusIcon.setStyle("-fx-font-size: 18px;");
                        
                        // Status and time in a VBox
                        VBox leftInfo = new VBox(2);
                        Label statusLabel = new Label(tx.getStatus().toString());
                        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                        Label timeLabel = new Label(tx.getDate().format(DateTimeFormatter.ofPattern("HH:mm")));
                        timeLabel.getStyleClass().add("date");
                        leftInfo.getChildren().addAll(statusLabel, timeLabel);
                        
                        // Amount (BTC and USD)
                        VBox amountBox = new VBox(2);
                        amountBox.setAlignment(Pos.CENTER_RIGHT);
                        HBox.setHgrow(amountBox, Priority.ALWAYS);
                        
                        Label btcAmount = new Label(CurrencyFormatter.formatBTCAmount(tx.getAmount()));
                        btcAmount.getStyleClass().addAll("amount", tx.getStatus().toString().toLowerCase());
                        btcAmount.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                        btcAmount.setMaxWidth(Double.MAX_VALUE);
                        btcAmount.setAlignment(Pos.CENTER_RIGHT);
                        
                        // Calculate USD amount (assuming 1 BTC = $98,000)
                        double btcPrice = 98000.0;
                        double usdAmount = Math.abs(tx.getAmount()) * btcPrice;
                        Label usdAmountLabel = new Label(String.format("≈ $%,.2f", usdAmount));
                        usdAmountLabel.getStyleClass().add("date");
                        usdAmountLabel.setMaxWidth(Double.MAX_VALUE);
                        usdAmountLabel.setAlignment(Pos.CENTER_RIGHT);
                        usdAmountLabel.setStyle("-fx-font-size: 11px;");
                        
                        amountBox.getChildren().addAll(btcAmount, usdAmountLabel);
                        
                        topRow.getChildren().addAll(statusIcon, leftInfo, amountBox);
                        
                        // Bottom row: Address
                        HBox bottomRow = new HBox(10);
                        bottomRow.setAlignment(Pos.CENTER_LEFT);
                        
                        String addressPrefix = tx.getStatus() == Status.RECEIVED ? "From: " : 
                                             tx.getStatus() == Status.SENT ? "To: " : "Address: ";
                        Label addressLabel = new Label(addressPrefix + (tx.getAddress() != null ? shortenAddress(tx.getAddress()) : "Unknown"));
                        addressLabel.getStyleClass().add("date");
                        addressLabel.setStyle("-fx-font-size: 11px; -fx-font-family: 'Consolas', 'Courier New', monospace; -fx-padding: 0 0 0 35;");
                        
                        bottomRow.getChildren().add(addressLabel);
                        
                        container.getChildren().addAll(topRow, bottomRow);
                        setGraphic(container);
                        getStyleClass().add("transaction-cell");
                    }
                }
            }
            
            private String shortenAddress(String address) {
                if (address == null || address.length() <= 20) {
                    return address;
                }
                return address.substring(0, 12) + "..." + address.substring(address.length() - 8);
            }
        });
    }
    
    private void loadTransactions() {
        filterTransactions();
    }
    
    private void filterTransactions() {
        List<TransactionModel> filtered = allTransactions;
        
        // Filter by status
        if (!currentFilter.equals("ALL")) {
            Status filterStatus = Status.valueOf(currentFilter);
            filtered = filtered.stream()
                .filter(tx -> tx.getStatus() == filterStatus)
                .collect(Collectors.toList());
        }
        
        // Filter by search text - search across all fields
        String searchText = searchField.getText().trim().toLowerCase();
        if (!searchText.isEmpty()) {
            filtered = filtered.stream()
                .filter(tx -> {
                    // Search in transaction ID
                    if (tx.getTxId().toLowerCase().contains(searchText)) {
                        return true;
                    }
                    // Search in amount (both BTC and USD)
                    String amountBTC = String.format("%.8f", Math.abs(tx.getAmount()));
                    double btcPrice = 98000.0;
                    String amountUSD = String.format("%.2f", Math.abs(tx.getAmount()) * btcPrice);
                    if (amountBTC.contains(searchText) || amountUSD.contains(searchText)) {
                        return true;
                    }
                    // Search in address
                    if (tx.getAddress() != null && tx.getAddress().toLowerCase().contains(searchText)) {
                        return true;
                    }
                    // Search in date (various formats)
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                    String date = tx.getDate().format(dateFormatter);
                    String time = tx.getDate().format(timeFormatter);
                    String fullDate = tx.getDate().format(fullFormatter).toLowerCase();
                    if (date.contains(searchText) || time.contains(searchText) || fullDate.contains(searchText)) {
                        return true;
                    }
                    // Search in hours and minutes separately
                    String hours = String.valueOf(tx.getDate().getHour());
                    String minutes = String.format("%02d", tx.getDate().getMinute());
                    if (hours.contains(searchText) || minutes.contains(searchText)) {
                        return true;
                    }
                    // Search in status
                    if (tx.getStatus().toString().toLowerCase().contains(searchText)) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }
        
        // Filter by date
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            filtered = filtered.stream()
                .filter(tx -> tx.getDate().toLocalDate().equals(selectedDate))
                .collect(Collectors.toList());
        }
        
        displayTransactions(filtered);
    }
    
    private void displayTransactions(List<TransactionModel> transactions) {
        transactionList.getItems().clear();
        
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // Group by day
        String currentDay = "";
        
        for (TransactionModel tx : transactions) {
            LocalDate txDate = tx.getDate().toLocalDate();
            String txDay = txDate.toString();
            
            // Add day separator
            if (!txDay.equals(currentDay)) {
                currentDay = txDay;
                
                // Determine the label text
                String dayLabel;
                if (txDate.equals(today)) {
                    dayLabel = "Today";
                } else if (txDate.equals(yesterday)) {
                    dayLabel = "Yesterday";
                } else {
                    dayLabel = txDate.format(dayFormatter);
                }
                
                // Create a dummy transaction with null txId to represent day separator
                // We'll use the address field to store the label text
                TransactionModel separator = new TransactionModel();
                separator.setDate(tx.getDate());
                separator.setAddress(dayLabel); // Store label in address field for separator
                transactionList.getItems().add(separator);
            }
            
            transactionList.getItems().add(tx);
        }
        
        if (transactions.isEmpty()) {
            // Show empty message - also using null txId to identify special cell
            TransactionModel emptyMessage = new TransactionModel();
            transactionList.getItems().add(emptyMessage);
        }
    }
    
    private String getStatusIcon(Status status) {
        switch (status) {
            case RECEIVED: return "↓";
            case SENT: return "↑";
            case PENDING: return "⏳";
            default: return "•";
        }
    }
    
    @FXML
    private void handleFilterAll() {
        currentFilter = "ALL";
        updateActiveFilter(filterAll);
        filterTransactions();
        updateClearButtonState();
    }
    
    @FXML
    private void handleFilterSent() {
        currentFilter = "SENT";
        updateActiveFilter(filterSent);
        filterTransactions();
        updateClearButtonState();
    }
    
    @FXML
    private void handleFilterReceived() {
        currentFilter = "RECEIVED";
        updateActiveFilter(filterReceived);
        filterTransactions();
        updateClearButtonState();
    }
    
    @FXML
    private void handleFilterPending() {
        currentFilter = "PENDING";
        updateActiveFilter(filterPending);
        filterTransactions();
        updateClearButtonState();
    }
    
    @FXML
    private void handleClearFilters() {
        // Clear search field
        searchField.clear();
        
        // Clear date picker
        datePicker.setValue(null);
        
        // Reset to "All" filter
        currentFilter = "ALL";
        updateActiveFilter(filterAll);
        
        // Refresh the transaction list
        filterTransactions();
        
        // Update button state
        updateClearButtonState();
    }
    
    private void updateClearButtonState() {
        boolean hasActiveFilters = !searchField.getText().trim().isEmpty() ||
                                   datePicker.getValue() != null ||
                                   !currentFilter.equals("ALL");
        
        if (hasActiveFilters) {
            // Red button when filters are active
            clearButton.setStyle("-fx-background-color: #d13438; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            // Gray button when no filters
            clearButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: normal; -fx-opacity: 0.6;");
        }
    }
    
    private void updateActiveFilter(Button activeButton) {
        filterAll.getStyleClass().remove("active");
        filterSent.getStyleClass().remove("active");
        filterReceived.getStyleClass().remove("active");
        filterPending.getStyleClass().remove("active");
        
        activeButton.getStyleClass().add("active");
    }
    
    private void showTransactionDetails(TransactionModel tx) {
        // Placeholder for transaction details dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transaction Details");
        alert.setHeaderText("Transaction Information");
        alert.setContentText(String.format(
            "Transaction ID: %s\nStatus: %s\nAmount: %s\nDate: %s",
            tx.getTxId(),
            tx.getStatus(),
            CurrencyFormatter.formatBTCAmount(tx.getAmount()),
            tx.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ));
        alert.showAndWait();
    }
}
