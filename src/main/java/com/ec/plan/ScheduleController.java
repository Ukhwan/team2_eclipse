package com.ec.plan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Controller
@PropertySource("classpath:/config/props/apiKey.properties")
public class ScheduleController {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Value("${clientId}")
	private String clientId;

	@Value("${clientSecret}")
	private String clientSecret;

	@RequestMapping("/showMap")
	public String showMap(Model model) {
		return "schedule";
	}
	
	@Autowired
	private OracleRepository oracleRepository;
	
	@ResponseBody
	@RequestMapping("/search")
	public ModelMap search(@RequestParam String addr) {
		ModelMap map = new ModelMap();
		try {
			String text = URLEncoder.encode(addr, "UTF-8");
			log.info(addr);

			String apiURL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + text;
			URL url = new URL(apiURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "text/plain");
			conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
			conn.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
				JsonArray addresses = jsonResponse.getAsJsonArray("addresses");

				if (addresses.size() > 0) {
					JsonObject firstAddress = addresses.get(0).getAsJsonObject();
					String x = firstAddress.get("x").getAsString();
					String y = firstAddress.get("y").getAsString();
					map.addAttribute("x", x);
					map.addAttribute("y", y);
				} else {
					String x = jsonResponse.get("x").getAsString();
					String y = jsonResponse.get("y").getAsString();
					map.addAttribute("x", x);
					map.addAttribute("y", y);
				}
			}
		} catch (Exception e) {
			log.error("search error: {}", e.getMessage());
			map.addAttribute("error", "Error occurred");
		}
		
		return map;
	}
	
	@ResponseBody
	@RequestMapping("/tour")
	public ModelMap tour(@RequestParam String x, @RequestParam String y) {
		ModelMap map = new ModelMap();
		String pkey = "g+INH4ICelRYTwvUPjujUIt/O1i9eSZAmhiCR9xJLT3v4P4aNkdXnRnDCkDGMKIdpXvJPsGJ9I5HTG6T2lmjkg==";
		
		try {
			
			List<Map<String, Object>> dbDataList = oracleRepository.getFromOracleDatabase();
			String key = URLEncoder.encode(pkey, "UTF-8");
			if (dbDataList.isEmpty()) {
				String apiURL="http://apis.data.go.kr/B551011/KorService1/categoryCode1?serviceKey=" + key +
		                "&numOfRows=2000&pageNo=1&MobileOS=ETC&MobileApp=AppTest&_type=json";
				
				handleApiResponse(apiURL,map);
			   
			} else {
			for (Map<String, Object> data : dbDataList) {
				
				String code = (String) data.get("cod");
				
				if (code != null && !code.isEmpty()) {
					String apiURL = buildApiUrl(key, code);
					
					// API 호출 및 응답 처리
					handleApiResponse(apiURL, map);
				}else {
					
					
				}
			}
			}
		} catch (Exception e) {
			log.error("에러 발생: {}", e.getMessage(), e);
		}

		return map;
	}

	private void handleApiResponse(String apiURL, ModelMap map) {
		try {
			// API 호출
			URL url = new URL(apiURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "text/plain");

			// 응답 코드 확인
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				// 응답 데이터 읽기
				try (BufferedReader in = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
					String inputLine;
					StringBuilder response = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}

					// 응답 데이터 처리
					JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
					JsonObject responseHeader = jsonResponse.getAsJsonObject("response").getAsJsonObject("header");
					if ("0000".equals(responseHeader.get("resultCode").getAsString())) {
						JsonObject responseBody = jsonResponse.getAsJsonObject("response").getAsJsonObject("body");
						JsonArray items = responseBody.getAsJsonObject("items").getAsJsonArray("item");

						if (items.size() > 0) {
							for (int i = 0; i < items.size(); i++) {
								JsonObject item = items.get(i).getAsJsonObject();
								String code = item.get("code").getAsString();
								String name = item.get("name").getAsString();
								map.addAttribute(code, name);

								oracleRepository.saveToOracleDatabase(code, name);
							}
						} else {
							System.out.println("데이터가 없습니다.");
						}
					} else {
						System.out.println("API 요청이 실패했습니다.");
					}
				}
			} else {
				System.out.println("API 요청이 실패했습니다. 응답 코드: " + responseCode);
			}
		} catch (IOException e) {
			log.error("에러 발생: {}", e.getMessage(), e);
		}
	}

	private String buildApiUrl(String key, String code) {
	    String apiURL;

	    if (code.length() == 3) {
	        apiURL = "http://apis.data.go.kr/B551011/KorService1/categoryCode1?serviceKey=" + key +
	                "&numOfRows=2000&pageNo=1&MobileOS=ETC&MobileApp=AppTest&cat1=" + code + "&_type=json";
	    } else {
	        apiURL = "http://apis.data.go.kr/B551011/KorService1/categoryCode1?serviceKey=" + key +
	                "&numOfRows=2000&pageNo=1&MobileOS=ETC&MobileApp=AppTest&cat1=" + code.substring(0, 3) +
	                "&cat2=" + code + "&_type=json";
	    }

	    return apiURL;
	}
}

@Repository
class OracleRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveToOracleDatabase(String cod, String nam) {
        String sql = "INSERT INTO cat (cod,nam) VALUES (?, ?)";
        jdbcTemplate.update(sql, cod, nam);
    }
    public List<Map<String, Object>> getFromOracleDatabase() {
    	String sql = "SELECT cod, nam FROM cat";
    	
        return jdbcTemplate.query(sql, new RowMapper<Map<String, Object>>() {
            @Override
            public Map<String, Object> mapRow(ResultSet resultSet, int i) throws SQLException {
                Map<String, Object> row = new HashMap<>();
                row.put("cod", resultSet.getString("cod"));
                row.put("nam", resultSet.getString("nam"));
                
                return row;
            }

    });
}
}