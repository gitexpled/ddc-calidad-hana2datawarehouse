package com.goplicity.ddcdw3.dw;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;



public class Hana2SqlSv {

	public Hana2SqlSv() {
	
	}
	
	public static JSONObject start(String especie) throws IOException, SQLException {
		JSONObject r = new JSONObject();
		
		JSONArray arrResult = new JSONArray();
		
		SqlSvConn s = new SqlSvConn();
		
		String path = "columnsddc.json";
        String contenido = getResourceFileContent(path);
        JSONObject jsonObject = new JSONObject(contenido);
        
		String centro ="Todo";
		//String especie ="CAROZO";
		String productor ="";
		String season ="2526";
		JSONArray especieColumnas = jsonObject.getJSONArray(especie+2);
		
		String url= "http://10.20.1.121:8003/sap/xsmc/getLoteUpdatedRfc.xsjs?cmd=getLoteUpdatedDW";
		String params ="";
		params += "&centro="+centro;
		params += "&especie="+especie; 
		params += "&productor="+productor;
		params += "&season="+season;
		params += "&TIPOINSPECCION=2";
		
		String curl = "curl "+url+params;
		JSONObject r1 = callCurl(curl);
		if(!isJSONArray(r1.optString("responseBody"))) {
			r.put("error",0);
			r.put("message","No Data");
			return r;
		}
		JSONArray arr = new JSONArray(r1.optString("responseBody"));
		for ( Object oLote : arr) {
			JSONObject res = new JSONObject();
            JSONObject jLote = (JSONObject)oLote;
            String lote = jLote.getString("LOTE");
            // DELETE
            String sqlDel = "DELETE FROM calidad_granel_" + especie + " WHERE LOTE ='" + lote + "'";
    		s.execute(sqlDel);

            // INSERT dinámico
            StringBuilder sqlText = new StringBuilder();
            sqlText.append("INSERT INTO calidad_granel_" + especie + " (");

            // columnas
            for (Object ocol : especieColumnas) {
            	String col = ocol.toString();
                String val = jLote.optString(col);
                if (val != null && !col.equals("")) {
                    String ncolumn;
					try {
						ncolumn = getNcolumn(col, especie);
						sqlText.append(ncolumn).append(",");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // tienes que implementar esto
                    
                }
            }
            sqlText.append(") VALUES (");

            // valores
            for (Object ocol : especieColumnas) {
            	String col = ocol.toString();
            	String val = jLote.optString(col);
                if (val != null && !col.equals("")) {
                    String valor = String.valueOf(val);
                    if (valor.contains(" %")) {
                        valor = valor.replace(" %", "");
                    }
                    String nvalue;
					try {
						nvalue = getNvalue(valor, col);
						sqlText.append("'").append(nvalue).append("',");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // tienes que implementar esto
                    
                }
            }
            sqlText.append(")");

            // limpiar comas
            String finalSql = sqlText.toString()
                    .replace(",)", ")")
                    .replace("'null'", "null")
                    .replace("''", "null");

            JSONObject resInsert = new JSONObject();
            resInsert= s.execute(finalSql);
            System.out.println(finalSql);
            // llamar servicio HTTP (similar a curl)
            if(resInsert.has("error") && resInsert.getInt("error")==0) {
            	String urlStr = "curl http://10.20.1.121:8003/sap/xsmc/getLoteUpdatedRfc.xsjs?cmd=updateLoteDW&LOTE=" + lote;
                JSONObject updateLote = callCurl(urlStr);

                res.put("updateLote", updateLote);
            }
            res.put("LOTE", lote);
            res.put("resInsert", resInsert);

            arrResult.put(res);
        }
		//s.close();
        r.put("results", arrResult);
        r.put("error",0);
		return r;
	}
	
	
	public static JSONObject callCurl(String curl) throws IOException {
		System.out.println(curl);
		JSONObject r = new JSONObject();
		Process process = Runtime.getRuntime().exec(curl);
		;
		
        String result = IOUtils.toString(process.getInputStream());
        System.out.println("Body: "+result);
        r.put("responseBody",result);
         
        JSONObject oresult= new JSONObject();
        try {
        	 oresult = new JSONObject(result);
        }catch(Exception ex) {
        	System.out.println(IOUtils.toString(process.getErrorStream()));
        	r.put("error",1);
        	r.put("Message","Respuesta sin contenido "+ex.getMessage());
        	return r;
        }
        
        r.put("error",0);
		r.put("message","OK");
        return r;
	}
	
	public static JSONObject callCurl(String[] curl) throws IOException {
		System.out.println(curl);
		JSONObject r = new JSONObject();
		Process process = Runtime.getRuntime().exec(curl);
		;
		
        String result = IOUtils.toString(process.getInputStream());
        //System.out.println("Body: "+result);
        r.put("responseBody",result);
        try {
        	 JSONObject oresult = new JSONObject(result);
        }catch(Exception ex) {
        	System.out.println(IOUtils.toString(process.getErrorStream()));
        	r.put("error",1);
        	r.put("Message","Respuesta sin contenido "+ex.getMessage());
        	return r;
        }
        
        r.put("error",0);
		r.put("message","OK");
        return r;
	}
	
	public static String getNcolumn(String col,String especie) {
		// Reemplazos básicos
		String rcol = col;
        col = col.replace("  ", " ");
        col = col.replace(" ", "_");
        col = col.replace(")", "_");
        col = col.replace("(", "");
        col = col.replace(">", "mayor");
        col = col.replace("<", "");
        col = col.replace(".", "_");
        col = col.replace("-", "_");
        col = col.replace(",", "_");

        // Normalizar múltiples "__"
        while (col.contains("__")) {
            col = col.replace("__", "_");
        }

        // Reglas por especie
        if ("ARANDANO".equalsIgnoreCase(especie)) {
        	col = col.replace("_de_", "");
            col = col.replace("_u_", "");
            col = col.replace("_o_", "");
            col = col.replace("_y_", "");
            col = col.replace(" ", "");
            col = col.replace("_", "");
        }

        if ("CAROZO".equalsIgnoreCase(especie)) {
            col = col.replace("_de_", "");
            col = col.replace("_u_", "");
            col = col.replace("_o_", "");
            col = col.replace("_y_", "");
            col = col.replace(" ", "");
            col = col.replace("_", "");
        }
        if ("DAGEN".equalsIgnoreCase(especie)) {
            col = col.replace("apr_Rech_", "apr_Rech");
        }

        // Si comienza con número → poner comillas
        if(col.length()==0) {
        	System.out.println(rcol);
        	return rcol;
        }
        char firstChar = col.charAt(0);
        if (Character.isDigit(firstChar)) {
            return "\"" + col + "\"";
        }

        return "\"" + col + "\"";
	}
	
	public static String getNvalue(String val, String col) {
        if (col.contains("Hora Revision ")) {
            // Separar por "."
            String[] arrNval = val.split("\\.");
            return arrNval[0];
        }

        // Reemplazar comillas simples por dobles comillas simples
        val = val.replace("'", "''");

        return val;
    }
	
	public static String getResourceFileContent(String archivo) {
		String contenido="";
        try (InputStream is = Hana2SqlSv.class.getClassLoader().getResourceAsStream(archivo)) {
            if (is == null) {
                throw new RuntimeException("No se encontró el archivo: " + archivo);
            }
            
            try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                contenido = scanner.useDelimiter("\\A").next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return contenido;
	}
	
	public static boolean isJSONArray(String data) {
		Object json = new JSONTokener(data).nextValue();
		if (json instanceof JSONObject) {
			return false;
		}else if (json instanceof JSONArray) {
			return true;
		}
		return false;
	}
}
