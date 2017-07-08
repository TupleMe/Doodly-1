/*******************************************************************************
 * Copyright (c) 2013 Gabriele Mariotti.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package tuple.me.lily.views.changelog.parser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tuple.me.lily.core.Objects;
import tuple.me.lily.views.changelog.internal.ChangeLog;
import tuple.me.lily.views.changelog.internal.ChangeLogRow;
import tuple.me.lily.views.changelog.internal.ChangeLogRowHeader;

public class XmlParser extends BaseParser {

    @NonNull
    private static final String TAG = "XmlParser";
    private int mChangeLogFileResourceId;

    private static final String TAG_CHANGELOG = "changelog";
    private static final String TAG_CHANGELOGVERSION = "changelogversion";
    private static final String TAG_CHANGELOGTEXT = "changelogtext";
    private static final String TAG_CHANGELOGBUG = "changelogbug";
    private static final String TAG_CHANGELOGIMPROVEMENT = "changelogimprovement";

    private static final String ATTRIBUTE_BULLETEDLIST = "bulletedList";
    private static final String ATTRIBUTE_VERSIONNAME = "versionName";
    private static final String ATTRIBUTE_VERSIONCODE = "versionCode";
    private static final String ATTRIBUTE_CHANGEDATE = "changeDate";
    private static final String ATTRIBUTE_CHANGETEXTTITLE = "changeTextTitle";

    @NonNull
    private static List<String> mChangeLogTags = new ArrayList<String>() {{
        add(TAG_CHANGELOGBUG);
        add(TAG_CHANGELOGIMPROVEMENT);
        add(TAG_CHANGELOGTEXT);
    }};

    public XmlParser(Context context) {
        super(context);
    }

    public XmlParser(Context context, int changeLogFileResourceId) {
        super(context);
        this.mChangeLogFileResourceId = changeLogFileResourceId;
    }

    @Nullable
    @Override
    public ChangeLog readChangeLogFile() throws Exception {

        ChangeLog chg = null;
        InputStream is = mContext.getResources().openRawResource(mChangeLogFileResourceId);
        Objects.checkNotNull(is, "Changelog not found");
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(is, null);
        parser.nextTag();
        chg = new ChangeLog();
        readChangeLogNode(parser, chg);
        is.close();
        return chg;
    }


    protected void readChangeLogNode(@Nullable XmlPullParser parser, @Nullable ChangeLog changeLog) throws Exception {

        if (parser == null || changeLog == null) return;

        parser.require(XmlPullParser.START_TAG, null, TAG_CHANGELOG);
        String bulletedList = parser.getAttributeValue(null, ATTRIBUTE_BULLETEDLIST);
        if (bulletedList == null || bulletedList.equals("true")) {
            changeLog.setBulletedList(true);
            super.bulletedList = true;
        } else {
            changeLog.setBulletedList(false);
            super.bulletedList = false;
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            if (tag.equals(TAG_CHANGELOGVERSION)) {
                readChangeLogVersionNode(parser, changeLog);
            }
        }
    }

    protected void readChangeLogVersionNode(@Nullable XmlPullParser parser, @NonNull ChangeLog changeLog) throws Exception {

        if (parser == null) return;

        parser.require(XmlPullParser.START_TAG, null, TAG_CHANGELOGVERSION);

        // Read attributes
        String versionName = parser.getAttributeValue(null, ATTRIBUTE_VERSIONNAME);
        String versionCodeStr = parser.getAttributeValue(null, ATTRIBUTE_VERSIONCODE);
        int versionCode = 0;
        if (versionCodeStr != null) {
            try {
                versionCode = Integer.parseInt(versionCodeStr);
            } catch (NumberFormatException ne) {
                Log.w(TAG, "Error while parsing versionCode.It must be a numeric value. Check you file.");
            }
        }
        String changeDate = parser.getAttributeValue(null, ATTRIBUTE_CHANGEDATE);

        ChangeLogRowHeader row = new ChangeLogRowHeader();
        row.setVersionName(versionName);
        row.setChangeDate(changeDate);
        changeLog.addRow(row);


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();

            if (mChangeLogTags.contains(tag)) {
                readChangeLogRowNode(parser, changeLog, versionName, versionCode);
            }
        }
    }

    private void readChangeLogRowNode(@Nullable XmlPullParser parser, @NonNull ChangeLog changeLog, String versionName, int versionCode) throws Exception {
        if (parser == null) return;
        String tag = parser.getName();
        ChangeLogRow row = new ChangeLogRow();
        row.setVersionName(versionName);
        row.setVersionCode(versionCode);

        String changeLogTextTitle = parser.getAttributeValue(null, ATTRIBUTE_CHANGETEXTTITLE);
        if (changeLogTextTitle != null)
            row.setChangeTextTitle(changeLogTextTitle);

        String bulletedList = parser.getAttributeValue(null, ATTRIBUTE_BULLETEDLIST);
        if (bulletedList != null) {
            if (bulletedList.equals("true")) {
                row.setBulletedList(true);
            } else {
                row.setBulletedList(false);
            }
        } else {
            row.setBulletedList(super.bulletedList);
        }

        if (parser.next() == XmlPullParser.TEXT) {
            String changeLogText = parser.getText();
            if (changeLogText == null)
                throw new IllegalArgumentException("ChangeLogText required in changeLogText node");
            row.parseChangeText(changeLogText);
            row.setType(tag.equalsIgnoreCase(TAG_CHANGELOGBUG) ? ChangeLogRow.BUGFIX : tag.equalsIgnoreCase(TAG_CHANGELOGIMPROVEMENT) ? ChangeLogRow.IMPROVEMENT : ChangeLogRow.DEFAULT);
            parser.nextTag();
        }
        changeLog.addRow(row);
    }

}
