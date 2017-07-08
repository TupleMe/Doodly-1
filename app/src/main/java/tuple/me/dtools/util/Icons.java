/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tuple.me.dtools.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseArray;

import java.io.File;
import java.util.HashMap;

import tuple.me.dtools.R;
import tuple.me.lily.util.MimeTypes;

public class Icons {
    private static HashMap<String, Integer> sMimeIconIds = new HashMap<String, Integer>();
    private static SparseArray<Bitmap> sMimeIcons = new SparseArray<>();

    private static void add(String mimeType, int resId) {
        if (sMimeIconIds.put(mimeType, resId) != null) {
            throw new RuntimeException(mimeType + " already registered!");
        }
    }

    private static void add(int resId, String... mimeTypes) {
        for (String type : mimeTypes) {
            add(type, resId);
        }
    }

    static {
        add(R.drawable.ic_android_mod,
                "application/vnd.android.package-archive"
        );

        add(R.drawable.ic_doc_music,
                "application/ogg",
                "application/x-flac"
        );
        add(R.drawable.ic_doc_general,
                "application/pgp-keys",
                "application/pgp-signature",
                "application/x-pkcs12",
                "application/x-pkcs7-certreqresp",
                "application/x-pkcs7-crl",
                "application/x-x509-ca-cert",
                "application/x-x509-user-cert",
                "application/x-pkcs7-certificates",
                "application/x-pkcs7-mime",
                "application/x-pkcs7-signature"
        );
        add(R.drawable.ic_doc_xml,
                "application/rdf+xml",
                "application/rss+xml",
                "application/x-object",
                "application/xhtml+xml",
                "text/css",
                "text/html",
                "text/xml",
                "text/x-c++hdr",
                "text/x-c++src",
                "text/x-chdr",
                "text/x-csrc",
                "text/x-dsrc",
                "text/x-csh",
                "text/x-haskell",
                "text/x-java",
                "text/x-literate-haskell",
                "text/x-pascal",
                "text/x-tcl",
                "text/x-tex",
                "application/x-latex",
                "application/x-texinfo",
                "application/atom+xml",
                "application/ecmascript",
                "application/json",
                "application/javascript",
                "application/xml",
                "text/javascript",
                "application/x-javascript"
        );
        add(R.drawable.ic_doc_zip,
                "application/mac-binhex40",
                "application/rar",
                "application/zip",
                "application/java-archive",
                "application/x-apple-diskimage",
                "application/x-debian-package",
                "application/x-gtar",
                "application/x-iso9660-image",
                "application/x-lha",
                "application/x-lzh",
                "application/x-lzx",
                "application/x-stuffit",
                "application/x-tar",
                "application/x-webarchive",
                "application/x-webarchive-xml",
                "application/gzip",
                "application/x-7z-compressed",
                "application/x-deb",
                "application/x-rar-compressed"
        );
        add(R.drawable.ic_doc_contact,
                "text/x-vcard",
                "text/vcard"
        );
        add(R.drawable.ic_doc_calender,
                "text/calendar",
                "text/x-vcalendar"
        );

        add(R.drawable.ic_doc_font,
                "application/x-font",
                "application/font-woff",
                "application/x-font-woff",
                "application/x-font-ttf"
        );
        add(R.drawable.ic_doc_image,
                "application/vnd.oasis.opendocument.graphics",
                "application/vnd.oasis.opendocument.graphics-template",
                "application/vnd.oasis.opendocument.image",
                "application/vnd.stardivision.draw",
                "application/vnd.sun.xml.draw",
                "application/vnd.sun.xml.draw.template",
                "image/jpeg",
                "image/png"
        );

        add(R.drawable.ic_doc_pdf,
                "application/pdf"
        );
        add(R.drawable.ic_doc_powerpoint,
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.openxmlformats-officedocument.presentationml.template",
                "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
                "application/vnd.stardivision.impress",
                "application/vnd.sun.xml.impress",
                "application/vnd.sun.xml.impress.template",
                "application/x-kpresenter",
                "application/vnd.oasis.opendocument.presentation"
        );
        add(R.drawable.ic_doc_word,
                "application/vnd.oasis.opendocument.spreadsheet",
                "application/vnd.oasis.opendocument.spreadsheet-template",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
                "application/vnd.stardivision.calc",
                "application/vnd.sun.xml.calc",
                "application/vnd.sun.xml.calc.template",
                "application/x-kspread"
        );
        add(R.drawable.ic_doc_word,
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
                "application/vnd.oasis.opendocument.text",
                "application/vnd.oasis.opendocument.text-master",
                "application/vnd.oasis.opendocument.text-template",
                "application/vnd.oasis.opendocument.text-web",
                "application/vnd.stardivision.writer",
                "application/vnd.stardivision.writer-global",
                "application/vnd.sun.xml.writer",
                "application/vnd.sun.xml.writer.global",
                "application/vnd.sun.xml.writer.template",
                "application/x-abiword",
                "application/x-kword"
        );
        add(R.drawable.ic_doc_file,
                "text/plain"
        );

        add(R.drawable.ic_doc_video,
                "application/x-quicktimeplayer",
                "application/x-shockwave-flash",
                "video/mp4"
        );
    }

    public static boolean isText(String name) {
        return isText(new File(name));
    }

    public static boolean isText(File file) {
        String mimeType = MimeTypes.getMimeType(file);
        Integer res = sMimeIconIds.get(mimeType);
        if (res != null && res == R.drawable.ic_doc_file) return true;
        if (mimeType != null && mimeType.contains("/")) {
            final String typeOnly = mimeType.split("/")[0];
            if ("text".equals(typeOnly)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVideo(String name) {
        return isVideo(new File(name));
    }

    public static boolean isVideo(File file) {
        String mimeType = MimeTypes.getMimeType(file);
        Integer res = sMimeIconIds.get(mimeType);
        if (res != null && res == R.drawable.ic_doc_video) return true;
        if (mimeType != null && mimeType.contains("/")) {
            final String typeOnly = mimeType.split("/")[0];
            if ("video".equals(typeOnly)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAudio(String name) {
        return isAudio(new File(name));
    }

    public static boolean isAudio(File file) {
        String mimeType = MimeTypes.getMimeType(file);
        Integer res = sMimeIconIds.get(mimeType);
        if (res != null && res == R.drawable.ic_doc_music) return true;
        if (mimeType != null && mimeType.contains("/")) {
            final String typeOnly = mimeType.split("/")[0];
            if ("audio".equals(typeOnly)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isCode(String name) {
        return isCode(new File(name));
    }

    public static boolean isCode(File file) {
        Integer res = sMimeIconIds.get(MimeTypes.getMimeType(file));
        if (res != null && res == R.drawable.ic_doc_xml) return true;
        return false;
    }


    public static boolean isArchive(String name) {
        return isArchive(new File(name));
    }

    public static boolean isArchive(File file) {
        Integer res = sMimeIconIds.get(MimeTypes.getMimeType(file));
        if (res != null && res == R.drawable.ic_doc_zip) return true;
        return false;
    }


    public static boolean isApk(String name) {
        return isApk(new File(name));
    }

    public static boolean isApk(File file) {
        Integer res = sMimeIconIds.get(MimeTypes.getMimeType(file));
        if (res != null && res == R.drawable.ic_android_mod) return true;
        return false;
    }


    public static boolean isPdf(String name) {
        return isPdf(new File(name));
    }

    public static boolean isPdf(File file) {
        Integer res = sMimeIconIds.get(MimeTypes.getMimeType(file));
        if (res != null && res == R.drawable.ic_doc_pdf) return true;
        return false;
    }

    public static boolean isPicture(String name) {
        return isPicture(new File(name));
    }

    public static boolean isPicture(File file) {
        Integer res = sMimeIconIds.get(MimeTypes.getMimeType(file));
        if (res != null && res == R.drawable.ic_doc_image) return true;
        return false;
    }


    public static boolean isGeneric(String name) {
        return isGeneric(new File(name));
    }

    public static boolean isGeneric(File file) {
        String mimeType = MimeTypes.getMimeType(file);
        if (mimeType == null) {
            return true;
        }
        Integer resId = sMimeIconIds.get(mimeType);
        if (resId == null) {
            return true;
        }
        return false;
    }

    public static BitmapDrawable loadMimeIcon(String path, boolean grid, final Resources res) {
        String mimeType = MimeTypes.getMimeType(new File(path));
        if (mimeType == null) {
            return loadBitmapDrawableById(res, R.drawable.ic_doc_general);
        }

        Integer resId = sMimeIconIds.get(mimeType);

        if (resId != null) {
            switch (resId) {
                case R.drawable.ic_android_mod:
                    if (grid) resId = R.drawable.ic_android_mod;
                    break;
                case R.drawable.ic_doc_image:
                    if (grid) resId = R.drawable.ic_doc_image;
                    break;
            }
            return loadBitmapDrawableById(res, resId);
        }

        final String typeOnly = mimeType.split("/")[0];

        if ("audio".equals(typeOnly)) {
            resId = R.drawable.ic_doc_music;
        } else if ("image".equals(typeOnly)) {
            if (grid) resId = R.drawable.ic_doc_image;
            else resId = R.drawable.ic_doc_image;
        } else if ("text".equals(typeOnly)) {
            resId = R.drawable.ic_doc_file;
        } else if ("video".equals(typeOnly)) {
            resId = R.drawable.ic_doc_video;
        }
        if (resId == null) {
            resId = R.drawable.ic_doc_general;
        }
        return loadBitmapDrawableById(res, resId);
    }

    private static BitmapDrawable loadBitmapDrawableById(Resources res, int resId) {
        Bitmap bitmap = sMimeIcons.get(resId);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(res, resId);
            sMimeIcons.put(resId, bitmap);
        }
        return new BitmapDrawable(res, bitmap);
    }


    public static int getIconId(File file) {
        String mimeType = MimeTypes.getMimeType(file);
        if (mimeType == null) {
            return R.drawable.ic_doc_general;
        }
        Integer resId = sMimeIconIds.get(mimeType);
        if (resId == null) {
            return R.drawable.ic_doc_general;
        }
        return resId;
    }
}
