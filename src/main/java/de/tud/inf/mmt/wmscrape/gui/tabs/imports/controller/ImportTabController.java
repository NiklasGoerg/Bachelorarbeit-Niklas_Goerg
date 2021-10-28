package de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;
import org.w3c.dom.Text;

@Component
public class ImportTabController {
    @FXML private TableView excelSheetTable;
    @FXML private Button newExcelSheetButton;
    @FXML private TextField pathField;
    @FXML private Button fileSelectionButton;
    @FXML private PasswordField passwordField;
    @FXML private TextField titleRowNrField;
    @FXML private TextField selectionColTitleField;
    @FXML private Button deleteEntryButton;
    @FXML private Button sheetPreviewButton;
    @FXML private TableView sheetPreviewTable;
    @FXML private TableView stockDataCorrelationTable;
    @FXML private TableView transactionCorrelationTable;
    @FXML private Button saveChangesButton;
    @FXML private Button importButton;

}
