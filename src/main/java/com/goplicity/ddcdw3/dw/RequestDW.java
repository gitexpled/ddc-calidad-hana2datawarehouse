package com.goplicity.ddcdw3.dw;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Path("/update-dw")
public class RequestDW {

	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get() throws IOException, SQLException {
		
		JSONObject r = new JSONObject();
		JSONArray jarr = new JSONArray();
		String[] arr = new String[]{"ARANDANO", "CAROZO", "PERA","KIWI","CEREZA","DAGEN","MANZANA"};
		
		for(String especie: arr) {
			JSONObject ritem = new JSONObject();
			ritem=Hana2SqlSv.start(especie);
			ritem.put("especie", especie);
			jarr.put(ritem);
			
		}
		r.put("error", 0);
		r.put("results", jarr);
		
        return r.toString();
    }
}
