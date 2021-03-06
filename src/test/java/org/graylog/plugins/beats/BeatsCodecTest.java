/**
 * This file is part of Graylog Beats Plugin.
 *
 * Graylog Beats Plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog Beats Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog Beats Plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.plugins.beats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.journal.RawMessage;
import org.graylog2.shared.bindings.providers.ObjectMapperProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BeatsCodecTest {
    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Configuration configuration;
    private ObjectMapper objectMapper;
    private BeatsCodec codec;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapperProvider().get();
        codec = new BeatsCodec(configuration, objectMapper);
    }

    @Test
    public void decodeReturnsNullIfPayloadCouldNotBeDecoded() throws Exception {
        assertThat(codec.decode(new RawMessage(new byte[0]))).isNull();
    }

    @Test
    public void decodeMessagesHandlesFilebeatMessages() throws Exception {
        final byte[] json = Resources.toByteArray(Resources.getResource("filebeat.json"));
        final RawMessage rawMessage = new RawMessage(json);
        final Message message = codec.decode(rawMessage);
        assertThat(message).isNotNull();
        assertThat(message.getMessage()).isEqualTo("TEST");
        assertThat(message.getSource()).isEqualTo("example.local");
        assertThat(message.getTimestamp()).isEqualTo(new DateTime(2016, 4, 1, 0, 0, DateTimeZone.UTC));
        assertThat(message.getField("facility")).isEqualTo("filebeat");
        assertThat(message.getField("file")).isEqualTo("/tmp/test.log");
        assertThat(message.getField("type")).isEqualTo("log");
        assertThat(message.getField("count")).isEqualTo(1);
        assertThat(message.getField("offset")).isEqualTo(0);
        @SuppressWarnings("unchecked")
        final List<String> tags = (List<String>) message.getField("tags");
        assertThat(tags).containsOnly("foobar", "test");
    }

    @Test
    public void decodeMessagesHandlesPacketbeatMessages() throws Exception {
        final byte[] json = Resources.toByteArray(Resources.getResource("packetbeat-dns.json"));
        final RawMessage rawMessage = new RawMessage(json);
        final Message message = codec.decode(rawMessage);
        assertThat(message).isNotNull();
        assertThat(message.getSource()).isEqualTo("example.local");
        assertThat(message.getTimestamp()).isEqualTo(new DateTime(2016, 4, 1, 0, 0, DateTimeZone.UTC));
        assertThat(message.getField("facility")).isEqualTo("packetbeat");
        assertThat(message.getField("type")).isEqualTo("dns");
    }

    @Test
    public void decodeMessagesHandlesTopbeatMessages() throws Exception {
        final byte[] json = Resources.toByteArray(Resources.getResource("topbeat-system.json"));
        final RawMessage rawMessage = new RawMessage(json);
        final Message message = codec.decode(rawMessage);
        assertThat(message).isNotNull();
        assertThat(message.getSource()).isEqualTo("example.local");
        assertThat(message.getTimestamp()).isEqualTo(new DateTime(2016, 4, 1, 0, 0, DateTimeZone.UTC));
        assertThat(message.getField("facility")).isEqualTo("topbeat");
        assertThat(message.getField("type")).isEqualTo("system");
    }

    @Test
    @Ignore("Ignored until JSON payload is added to test assets")
    public void decodeMessagesHandlesWinlogbeatMessages() throws Exception {
        final byte[] json = Resources.toByteArray(Resources.getResource("winlogbeat.json"));
        final RawMessage rawMessage = new RawMessage(json);
        final Message message = codec.decode(rawMessage);
        assertThat(message).isNotNull();
    }

    @Test
    public void decodeMessagesHandleGenericBeatMessages() throws Exception {
        final byte[] json = Resources.toByteArray(Resources.getResource("generic.json"));
        final RawMessage rawMessage = new RawMessage(json);
        final Message message = codec.decode(rawMessage);
        assertThat(message).isNotNull();
        assertThat(message.getSource()).isEqualTo("unknown");
        assertThat(message.getTimestamp()).isEqualTo(new DateTime(2016, 4, 1, 0, 0, DateTimeZone.UTC));
        assertThat(message.getField("facility")).isEqualTo("genericbeat");
    }
}