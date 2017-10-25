package com.trilaterator;

import java.io.IOException;

/**
 * Created by rajeshmaheswaran on 15/06/17.
 */
public interface StepListener {
    public void step(long timeNs) throws IOException;

}
