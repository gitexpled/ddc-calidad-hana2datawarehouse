package com.goplicity.ddcdw3.dw;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.json.JSONArray;
import org.json.JSONObject;

public class SqlSvConn {
	
	public Connection conn;
	public SqlSvConn(){
		String url = "jdbc:sqlserver://ddcbi.database.windows.net:1433;databaseName=ddc_datawarehouse;encrypt=false";
        String user = "user_maf";   // tu usuario
        String password = "jBdbAoxv2@pLd"; // tu password
        JSONObject result = new JSONObject();
        try{
        	Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        	conn = DriverManager.getConnection(url, user, password);
        	result.put("message","✅ Conexión exitosa a SQL Server!");
        	System.out.println(" Conexión exitosa a SQL Server");

        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public JSONObject executeQuery(String Sql) throws SQLException {

		JSONObject result = new JSONObject();
		JSONArray arr = new JSONArray();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(Sql);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
        	JSONObject item = new JSONObject();

            for (int i = 1; i <= columnCount; i++) {
                String nombreColumna = metaData.getColumnLabel(i);
                int tipo = metaData.getColumnType(i);
                Object valor;

                switch (tipo) {
                    case Types.VARCHAR:
                    case Types.NVARCHAR:
                    case Types.CHAR:
                    case Types.LONGVARCHAR:
                        valor = rs.getString(i);
                        break;

                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.TINYINT:
                    case Types.BIGINT:
                        valor = rs.getLong(i);
                        if (rs.wasNull()) valor = null;
                        break;

                    case Types.DECIMAL:
                    case Types.NUMERIC:
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                        valor = rs.getBigDecimal(i);
                        break;

                    case Types.DATE:
                    case Types.TIMESTAMP:
                    case Types.TIME:
                        valor = rs.getTimestamp(i);
                        break;

                    default:
                        // Para otros tipos, lo devolvemos como String genérico
                        valor = rs.getObject(i);
                }

                item.put(nombreColumna, valor);
            }
            arr.put(item);
        }
        result.put("data", arr);
        return result;
	}
	
	public JSONObject execute(String Sql) throws SQLException {

		JSONObject result = new JSONObject();
		//JSONArray arr = new JSONArray();
		try {
			
			PreparedStatement stmt = conn.prepareStatement(Sql);
            int filasAfectadas = stmt.executeUpdate();
        	result.put("error", 0);
        	result.put("message", "Filas fectadas: "+filasAfectadas);

	        } catch (Exception e) {
	            e.printStackTrace();
	            result.put("error", 1);
	        	result.put("message", e.getMessage());
	        	result.put("stackTrace", e.getStackTrace());
	            
	        }
       //result.put("data", arr);
        return result;
	}
	
	public void close() throws SQLException {
		conn.close();
	}
}
