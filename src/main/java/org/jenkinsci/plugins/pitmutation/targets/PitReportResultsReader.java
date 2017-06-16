package org.jenkinsci.plugins.pitmutation.targets;

import hudson.FilePath;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.pitmutation.MutationReport;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

class PitReportResultsReader {

    private static final String ROOT_REPORT_FOLDER = "mutation-report";
    private static final String DEFAULT_MODULE_NAME = "module";


    Map<String, MutationReport> readReports(File rootDir, PrintStream buildLogger) {
        Map<String, MutationReport> reports = new HashMap<String, MutationReport>();

        try {
            FilePath[] files = new FilePath(rootDir).list(ROOT_REPORT_FOLDER + "/**/mutations.xml");
//            buildLogger.println("filesów znaleziono " + files.length);
            if (files.length < 1) {
                buildLogger.println("Could not find " + ROOT_REPORT_FOLDER + "/**/mutations.xml in " + rootDir);
            }
            for (int i = 0; i < files.length; i++) {
                if (files.length == 1) {
                    buildLogger.println("Creating report for single module project, file: " + files[i].getRemote());
                    reports.put(DEFAULT_MODULE_NAME, MutationReport.create(files[i].read()));
                } else {
                    buildLogger.println("Creating report for multi module project, file: " + files[i].getRemote());
                    reports.put(extractModuleName(files[i].getRemote()), MutationReport.create(files[i].read()));
                }
            }
        } catch (IOException e) {
            buildLogger.println("IO EXCEPTION");
            e.printStackTrace();
        } catch (SAXException e) {
            buildLogger.println("SAX EXCEPTION");
            e.printStackTrace();
        } catch (InterruptedException e) {
            buildLogger.println("Interrupted EXCEPTION");
            e.printStackTrace();
        }
//        buildLogger.println(" ****** Reports siezer found" + reports.size());
        return reports;
    }

    String extractModuleName(String remote) { //FIXME if single module returns empty string
        String pathToMutationReport = StringUtils.substringBeforeLast(remote, File.separator);
        return StringUtils.substringAfterLast(pathToMutationReport, File.separator + ROOT_REPORT_FOLDER + File.separator);
    }

    /*
    void publishReports(FilePath[] reports, FilePath buildTarget) {
        if (isMultiModuleProject(reports)) {
            copyMultiModulesReportsFiles(reports, buildTarget);
        } else {
            copySingleModuleReportFiles(reports, buildTarget);
        }
    }
     */


}
