package org.iw11.driver.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Optional;

public class TokenManager {

    private static final String FILENAME = "storage.store";

    private Context applicationContext;

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        writeToFile(this.token);
    }

    public TokenManager(Context applicationContext) {
        this.applicationContext = applicationContext;
        token = readFromFile();
    }

    private String readFromFile() {
        File file = new File(applicationContext.getFilesDir(), FILENAME);
        if (!file.exists())
            return null;

        StringBuilder builder = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file))) {
            int c = 0;
            while ((c = reader.read()) != -1)
                builder.append((char) c);
        } catch (IOException e) {
        }

        return builder.toString();
    }

    private void writeToFile(String line) {
        File file = new File(applicationContext.getFilesDir(), FILENAME);
        if (file.exists()) {

            if (line == null) {
                file.delete();
                return;
            }

            boolean created;
            try {
                created = file.createNewFile();
            } catch (IOException e) {
                return;
            }
            if (!created)
                return;
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file))) {
            writer.append(line);
            writer.flush();
        } catch (IOException e) {
        }
    }
}
