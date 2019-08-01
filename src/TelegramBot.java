import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.HttpHost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TelegramBot {
    private final String endpoint = "https://api.telegram.org/bot";
    private final String token;

    public TelegramBot(String token, String proxyHost, Integer proxyPort) {
        this.token = token;
        Unirest.setProxy(new HttpHost(proxyHost, proxyPort));
    }

    public HttpResponse<JsonNode> sendMessage(Integer chatId, String text) throws UnirestException {
        return Unirest.post(endpoint + token + "/sendMessage").field("chat_id", chatId).field("text", text).asJson();
    }

    public HttpResponse<JsonNode> getUpdates(Integer offset) throws UnirestException {
        return Unirest.post(endpoint + token + "/getUpdates").field("offset", offset).asJson();
    }

    public void run() throws UnirestException {
        int last_update_id = 0;
        staticLotus mylotus = new staticLotus();
        HttpResponse<JsonNode> response;

        System.err.println(Calendar.getInstance().getTime() + " Waiting for updates...");

        while (true) {
            try {
                response = getUpdates(last_update_id++);
                if (response.getStatus() == 200) {
                    try {
                        JSONArray responses = response.getBody().getObject().getJSONArray("result");
                        if (responses.isNull(0)) {
                            try {
                                synchronized (this) {
                                    this.wait(500);
                                }
                            } catch (Exception e) {
                                //System.err.println(Calendar.getInstance().getTime() + " " + e.getMessage());
                            }
                            continue;
                        } else {
                            last_update_id = responses.getJSONObject(responses.length() - 1).getInt("update_id") + 1;
                        }

                        for (int i = 0; i < responses.length(); i++) {
                            System.err.println(Calendar.getInstance().getTime() + " New message");

                            JSONObject update = responses.getJSONObject(i);

                            if (update.has("message")) {
                                JSONObject message = update.getJSONObject("message");
                                JSONObject chat = message.getJSONObject("chat");
                                int chat_id = chat.getInt("id");

                                if (message.has("text")) {
                                    String text = message.getString("text");

                                    if (text.contains("/start")) {
                                        sendMessage(chat_id, String.valueOf(chat_id));
                                    } else if (message.has("reply_to_message")) {
                                        JSONObject reply_to_message = message.getJSONObject("reply_to_message");

                                        if (reply_to_message.has("text")) {
                                            String origMsg = reply_to_message.getString("text");
                                            Pattern pattern = Pattern.compile("nr\\.0075\\.pfr\\.ru/(.+?)/(\\w+?)/0/(\\w+)$");
                                            Matcher matcher = pattern.matcher(origMsg);

                                            if (matcher.find()) {
                                                String server = matcher.group(1);
                                                String replicaID = matcher.group(2);
                                                String docUNID = matcher.group(3);

                                                int ret = mylotus.postComment(server, replicaID, docUNID, text, String.valueOf(chat_id));
                                                if (ret == 0) {
                                                    sendMessage(chat_id, "Успешно \uD83D\uDC4C");
                                                } else {
                                                    sendMessage(chat_id, "Ошибка! \uD83D\uDE27");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        System.err.println(Calendar.getInstance().getTime() + " Invalid JSON");
                    }
                }
            } catch (UnirestException e) {
                System.err.println(Calendar.getInstance().getTime() + " " + e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException eInterrupted) {
                    System.err.println(Calendar.getInstance().getTime() + " " + eInterrupted.getMessage());
                }

            }
        }
    }
}