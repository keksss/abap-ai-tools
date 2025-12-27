package com.keksss.abap.ai.ui.handlers;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Property tester to check if an object is an AbapFeedEntry
 */
public class AbapFeedEntryPropertyTester extends PropertyTester {

    private static final String PROPERTY_IS_ABAP_FEED_ENTRY = "isAbapFeedEntry";
    private static final String ABAP_FEED_ENTRY_CLASS_NAME = "com.sap.adt.feedreader.internal.feed.AbapFeedEntry";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (PROPERTY_IS_ABAP_FEED_ENTRY.equals(property)) {
            return receiver != null &&
                    ABAP_FEED_ENTRY_CLASS_NAME.equals(receiver.getClass().getName());
        }
        return false;
    }
}
