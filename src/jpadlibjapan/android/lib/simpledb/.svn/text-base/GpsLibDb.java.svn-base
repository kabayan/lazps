package jpadlibjapan.android.lib.simpledb;

import android.content.Context;

public class GpsLibDb extends SimpleDbHelper {
	public Tables table = new Tables();

	public class Tables {
		public geo_table geo_table = new geo_table();
	}

	public class geo_table {
		@att("primary key autoincrement")
		public Integer _id = 1;
		String _ID = "_id";

		public Double time = 0D;
		String TIME = "time";
		public Double lat = 0D;
		String LAT = "lat";
		public Double lon = 0D;
		String LON = "lon";
		public Double acc = 0D;
		String ACC = "acc";

		public String address = "";
		String ADRESS = "address";
	}

	public GpsLibDb(Context context) {
		dbname = "gpslibdb";
		DATABASE_VERSION = 3;
		mTableDefine = table;
		create(context);
	}

}
