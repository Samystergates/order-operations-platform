//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.web.appts.services;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.DocFlavor.BYTE_ARRAY;
import javax.print.DocFlavor.INPUT_STREAM;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.Sides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppPrintService {
    private static final Logger logger = LoggerFactory.getLogger(AppPrintService.class);

    public AppPrintService() {
    }

    public static boolean PerformPrint(String printerName, byte[] pdfBytes) {
        String printerIPAddress = "192.168.18.131";
        PrintService printer = findPrinter(printerIPAddress, printerName);
        if (printer != null) {
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            attributes.add(MediaSizeName.ISO_A4);
            attributes.add(Sides.ONE_SIDED);
            attributes.add(new Copies(1));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
            Doc doc = new SimpleDoc(inputStream, INPUT_STREAM.AUTOSENSE, (DocAttributeSet)null);
            DocPrintJob printJob = printer.createPrintJob();

            try {
                printJob.print(doc, attributes);
            } catch (PrintException var9) {
                PrintException e = var9;
                logger.error("Failed to print order PDF", e);
                return false;
            }

            logger.info("Order PDF sent to printer {}", printerName);
            return true;
        } else {
            logger.warn("Printer {} was not found", printerName);
            return false;
        }
    }

    public static boolean PerformPrint(String printerName, byte[] pdfBytes, boolean isZebra) {
        String printerIPAddress = "192.168.18.131";
        String zplData = "^XA^FO50,50^ADN,36,20^FDHello, World!^FS^XZ";

        byte[] zplBytes;
        try {
            zplBytes = zplData.getBytes("UTF-8");
        } catch (UnsupportedEncodingException var12) {
            UnsupportedEncodingException e = var12;
            logger.error("Failed to encode ZPL data", e);
            return false;
        }

        PrintService printer = findPrinter(printerIPAddress, printerName);
        if (printer != null) {
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            Doc doc = new SimpleDoc(zplBytes, BYTE_ARRAY.AUTOSENSE, (DocAttributeSet)null);
            DocPrintJob printJob = printer.createPrintJob();

            try {
                printJob.print(doc, attributes);
            } catch (PrintException var11) {
                PrintException e = var11;
                logger.error("Failed to print Zebra label", e);
                return false;
            }

            logger.info("Zebra label sent to printer {}", printerName);
            return true;
        } else {
            logger.warn("Printer {} was not found", printerName);
            return false;
        }
    }

    private static PrintService findPrinter(String printerIPAddress, String printerName) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices((DocFlavor)null, (AttributeSet)null);
        PrintService[] var3 = services;
        int var4 = services.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            PrintService service = var3[var5];
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }

        return null;
    }
}
