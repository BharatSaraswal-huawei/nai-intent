/*
 * Copyright 2015-present Open Networking Laboratory
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
 */
package org.onosproject.nai.web.codec;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.ConnectivityIntent;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.Intent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Nai intent JSON codec.
 */
public final class NaiIntentCodec extends JsonCodec<Intent> {

    private static final String PRIORITY = "priority";
    private static final String CONSTRAINTS = "constraints";
    private static final String SELECTOR = "selector";
    private static final String TREATMENT = "treatment";

    @Override
    public ObjectNode encode(Intent in, CodecContext context) {
        checkNotNull(in, "intent cannot be null");

        ConnectivityIntent intent = (ConnectivityIntent) in;

        final JsonCodec<Intent> intentCodec = context.codec(Intent.class);
        final ObjectNode result = intentCodec.encode(intent, context);

        if (intent.selector() != null) {
            final JsonCodec<TrafficSelector> selectorCodec =
                    context.codec(TrafficSelector.class);
            result.set(SELECTOR, selectorCodec.encode(intent.selector(), context));
        }

        if (intent.treatment() != null) {
            final JsonCodec<TrafficTreatment> treatmentCodec =
                    context.codec(TrafficTreatment.class);
            result.set(TREATMENT, treatmentCodec.encode(intent.treatment(), context));
        }

        result.put(PRIORITY, intent.priority());

        if (intent.constraints() != null) {
            final ArrayNode jsonConstraints = result.putArray(CONSTRAINTS);

            if (intent.constraints() != null) {
                final JsonCodec<Constraint> constraintCodec =
                        context.codec(Constraint.class);
                for (final Constraint constraint : intent.constraints()) {
                    final ObjectNode constraintNode =
                            constraintCodec.encode(constraint, context);
                    jsonConstraints.add(constraintNode);
                }
            }
        }

        return result;
    }
}
