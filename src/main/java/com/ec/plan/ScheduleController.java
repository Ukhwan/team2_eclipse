package com.ec.plan;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@PropertySource("classpath:apiKey.properties")
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
            log.error("error: {}", e.getMessage());
            map.addAttribute("error", "Error occurred");
        }
        return map;
    }

    @Autowired
    private OracleRepository oracleRepository;

    @ResponseBody
    @RequestMapping("/tour")
    public ModelMap tour(@RequestParam String x, @RequestParam String y) {
        ModelMap map = new ModelMap();
        try {
            String pkey = "g+INH4ICelRYTwvUPjujUIt/O1i9eSZAmhiCR9xJLT3v4P4aNkdXnRnDCkDGMKIdpXvJPsGJ9I5HTG6T2lmjkg==";
            String key = URLEncoder.encode(pkey, "UTF-8");
            String apiURL = "http://apis.data.go.kr/B551011/KorService1/categoryCode1?serviceKey=" + key +
                    "&numOfRows=2000&pageNo=1&MobileOS=ETC&MobileApp=AppTest&_type=json";
            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "text/plain");
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                log.info("API 응답: " + response.toString());
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonObject responseHeader = jsonResponse.getAsJsonObject("response").getAsJsonObject("header");
                if ("0000".equals(responseHeader.get("resultCode").getAsString())) {
                    JsonObject responseBody = jsonResponse.getAsJsonObject("response").getAsJsonObject("body");
                    JsonArray items = responseBody.getAsJsonObject("items").getAsJsonArray("item");
                    System.out.println(items.size());
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
        } catch (Exception e) {
            log.error("error: {}", e.getMessage());
        }
        System.out.println("안녕");
        return map;
    }
}

@Repository
class OracleRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveToOracleDatabase(String cod, String nam) {
        String sql = "INSERT INTO cat3 (cod,nam) VALUES (?, ?)";
        jdbcTemplate.update(sql.toUpperCase(), cod, nam);
    }
}
