package com.ipeirotis.gal.engine.rpt;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileReportTarget extends ReportTarget {
	public FileReportTarget(String path) throws IOException {
		super(new FileOutputStream(path));
	}

}
