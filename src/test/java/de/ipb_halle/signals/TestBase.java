/*
 * IPB Signals client
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.signals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class TestBase {


    /**
     * @param stream the InputStream as obtained from Class.getResourceAsStream()
     * @return the stream content 
     */
    public static String readStream(InputStream stream) {
        StringBuilder sb = new StringBuilder();

        try (InputStreamReader streamReader = new InputStreamReader(stream);
                BufferedReader bufReader = new BufferedReader(streamReader)) {
            String line = bufReader.readLine();
            while (line != null) {
                sb.append(line);
                line = bufReader.readLine();
            }
        } catch(IOException e) {
            return "";
        }
        return sb.toString();
    }
}
