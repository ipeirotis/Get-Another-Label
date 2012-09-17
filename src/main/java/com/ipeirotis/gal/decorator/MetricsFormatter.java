package com.ipeirotis.gal.decorator;

import com.ipeirotis.utils.Utils;

public abstract class MetricsFormatter {
	public abstract String format(Double result);

	public static final MetricsFormatter PERCENT_FORMATTER = new MetricsFormatter() {
		@Override
		public
		String format(Double result) {
			if (null == result || Double.isNaN(result))
				return "N/A";

			return String.format("%s%%", Utils.round(100 * result, 2));
		}
	};
}
