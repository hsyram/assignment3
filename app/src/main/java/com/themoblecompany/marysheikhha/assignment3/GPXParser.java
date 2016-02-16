package com.themoblecompany.marysheikhha.assignment3;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will process gpx files in assets folder and return list of lat and long for each marker.
 * I tried to use existent GPXParser libraries such as <a href="https://github.com/urizev/j4gpx">j4gpx</a>
 * or <a href="https://github.com/ticofab/android-gpx-parser">Android GPX Parser</a> but they were not match
 * with our gpx files, so I wrote custom parser using
 * <a href="http://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html">XmlPullParser</a>
 * <br/>
 * Created by mary on 2/15/16.
 */
public class GPXParser {
    //listener for sending result
    OnParseFinishListener mListener;
    //application Context
    Context mContext;

    // constructor
    public GPXParser(OnParseFinishListener listener, Context context) {
        this.mListener = listener;
        this.mContext = context;
    }

    /**
     * start parsing files and send result using handler
     */
    public void start() {
        if (mListener == null)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                //array of file names
                String[] files = {"A2Li.gpx", "A2LiToA9Re.gpx", "A2Re.gpx", "A2ReLong.gpx",
                        "A2ReTest.gpx", "A12.gpx", "afslag.gpx", "n402Li.gpx",
                        "n402Re.gpx", "N419Li.gpx", "oprit.gpx"};

                List result;
                InputStream in;

                for (String file : files) {
                    try {
                        //read file
                        in = mContext.getAssets().open(file);
                        //parse file
                        result = parse(in);
                        //send result
                        mListener.showMarkers(result);
                    } catch (IOException | XmlPullParserException e) {
                        e.printStackTrace();
                    }
                }
                //send process finished.
                mListener.finish();
            }
        }).start();
    }

    /**
     * init parser and call for parsing file
     *
     * @param in inputStream of gpx file
     * @return list of lat and long. see {@link WPT}
     * @throws XmlPullParserException
     * @throws IOException
     */
    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readGpx(parser);
        } finally {
            in.close();
        }
    }

    /**
     * parse gpx file and return list of lat and long on that file
     *
     * @param parser XmlPullParser
     * @return list of lat and long. see {@link WPT}
     * @throws XmlPullParserException
     * @throws IOException
     */
    private List readGpx(XmlPullParser parser) throws XmlPullParserException, IOException {
        List wpts = new ArrayList();

        //start to reading xml file
        parser.require(XmlPullParser.START_TAG, null, "gpx");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.getName().equals("wpt")) {
                try {
                    String lat = parser.getAttributeValue(null, "lat");
                    String lon = parser.getAttributeValue(null, "lon");
                    if (lat != null && lon != null)
                        wpts.add(new WPT(Double.parseDouble(lat), Double.parseDouble(lon)));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            eventType = parser.next();
        }
        return wpts;
    }

    /**
     * model contain lat and long for each milemarker
     */
    public static class WPT {
        public final double lat;
        public final double lon;

        private WPT(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    /**
     * interface for sending result to caller
     */
    public interface OnParseFinishListener {
        /**
         * will be called when reading of one file is done.
         *
         * @param markers list of lat and long. see {@link WPT}
         */
        void showMarkers(List markers);

        /**
         * will be called when reading whole files is done.
         */
        void finish();
    }
}
