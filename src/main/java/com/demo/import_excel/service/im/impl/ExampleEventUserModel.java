package com.demo.import_excel.service.im.impl;

import com.demo.import_excel.repository.entity.TempEntity;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class ExampleEventUserModel {
    public List<TempEntity> processOneSheet(String filename) throws Exception {
        log.info("Start loading from excel file");
        List<String> headers = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();
        List<TempEntity> tempEntities = new ArrayList<>();

        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = (SharedStringsTable) r.getSharedStringsTable();
        XMLReader parser = fetchSheetParser(sst, headers, data);
        // To look up the Sheet Name / Sheet Order / rID,
        //  you need to process the core Workbook stream.
        // Normally it's of the form rId# or rSheet#
        InputStream sheet2 = r.getSheet("rId1");
        InputSource sheetSource = new InputSource(sheet2);
        parser.parse(sheetSource);
        sheet2.close();

        return mapping(headers, data);
    }

    public List<TempEntity> processAllSheets(String filename) throws Exception {

        List<String> headers = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();
        List<TempEntity> tempEntities = new ArrayList<>();

        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = (SharedStringsTable) r.getSharedStringsTable();
        XMLReader parser = fetchSheetParser(sst,headers, data);
        Iterator<InputStream> sheets = r.getSheetsData();
        while (sheets.hasNext()) {
            System.out.println("Processing new sheet:\n");
            InputStream sheet = sheets.next();
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
            sheet.close();
        }

        return tempEntities;
    }

    List<TempEntity> mapping(List<String> headers, List<List<String>> data) {
        List<TempEntity> tempEntities;
        tempEntities = data.stream().map(row -> {
            TempEntity tempEntity = new TempEntity();
            tempEntity.setId(Long.parseLong(row.get(0)));
            tempEntity.setFirstName(row.get(1));
            tempEntity.setLastName(row.get(2));
            tempEntity.setEmail(row.get(3));
            tempEntity.setGender(row.get(4));
            tempEntity.setIpAddress(row.get(5));
            return tempEntity;
        }).collect(Collectors.toList());
        return tempEntities;
    }

    public XMLReader fetchSheetParser(SharedStringsTable sst, List<String> headers, List<List<String>> data) throws SAXException, ParserConfigurationException {
        XMLReader parser = XMLHelper.newXMLReader();
        ContentHandler handler = new SheetHandler(sst, headers, data);
        parser.setContentHandler(handler);
        return parser;
    }

    /**
     * See org.xml.sax.helpers.DefaultHandler javadocs
     */
    private static class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString;

        List<String> headers;
        List<List<String>> data;

        int column = 0;
        int row = 0;
        boolean isHeader;
        boolean isBody;

        private SheetHandler(SharedStringsTable sst, List<String> headers, List<List<String>> data) {
            this.sst = sst;
            this.headers = headers;
            this.data = data;
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        @Override
        public void startElement(String uri, String localName, String name,
                                 Attributes attributes) throws SAXException {
            // c => cell
            if (name.equals("c")) {
                // Print the cell reference
                String r = attributes.getValue("r");
                //handle header
                if (r.endsWith("1") && r.length() == 2) {
                    isHeader = true;
                    isBody = false;
                } else {
                    isHeader = false;
                    isBody = true;
                }
                // Figure out if the value is an index in the SST
                String cellType = attributes.getValue("t");
                if (cellType != null && cellType.equals("s")) {
                    nextIsString = true;
                } else {
                    nextIsString = false;
                }
            }
            // Clear contents cache
            lastContents = "";
        }

        @Override
        public void endElement(String uri, String localName, String name)
                throws SAXException {
            // Process the last contents as required.
            // Do now, as characters() may be called more than once
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = sst.getItemAt(idx).getString();
                nextIsString = false;
            }
            // v => contents of a cell
            // Output after we've seen the string contents
            if (!name.equals("v")) {
                return;
            }
            if (isHeader) {
                headers.add(lastContents);
            }
            if (isBody) {
                if (data.size() < row + 1) {
                    data.add(new ArrayList<>());
                }
                List<String> rowData = data.get(row);
                rowData.add(lastContents);
                if (rowData.size() == headers.size()) {
                    row++;
                }
            }
        }

        public void characters(char[] ch, int start, int length) {
            lastContents += new String(ch, start, length);
        }

    }
}
