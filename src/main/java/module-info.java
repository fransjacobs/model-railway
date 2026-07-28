/*
 * Copyright 2026 frans.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

open module jcs {
  requires java.desktop;        // java.awt.*, javax.swing.*
  requires java.logging;        // java.util.logging.*
  requires java.sql;            // java.sql.*  (needed by H2, Hikari)
  requires java.naming;         // javax.naming.* (needed by some persistence)
  requires java.xml;            // javax.xml.*, org.xml.sax.*
  requires java.net.http;       // java.net.http.* (if used)
  requires jdk.unsupported;     // sun.misc.Unsafe (needed by several libs)
  requires java.prefs; 
  
  
  
  requires org.tinylog.api;
  requires org.tinylog.impl;
  requires com.h2database;
  requires org.apache.commons.lang3;
  requires org.beryx.awt.color;
  requires org.slf4j.nop;
  requires org.slf4j;
  requires norm;
  requires jakarta.persistence;
  requires com.zaxxer.hikari;
  requires org.json;
  requires com.twelvemonkeys.imageio.batik;
  requires com.twelvemonkeys.imageio.core;
  requires com.twelvemonkeys.common.lang;
  requires com.twelvemonkeys.common.io;
  requires com.twelvemonkeys.common.image;
  requires org.apache.xmlgraphics.batik.transcoder;
  requires org.apache.xmlgraphics.batik.anim;
  requires org.apache.xmlgraphics.batik.css;
  requires org.apache.xmlgraphics.batik.ext;
  requires org.apache.xmlgraphics.batik.parser;
  requires org.apache.xmlgraphics.batik.svgdom;
  requires org.apache.xmlgraphics.batik.awt.util;
  requires org.apache.xmlgraphics.commons;
  requires org.apache.commons.io;
  requires org.apache.commons.logging;
  requires org.apache.xmlgraphics.batik.bridge;
  requires org.apache.xmlgraphics.batik.script;
  requires org.apache.xmlgraphics.batik.dom;
  requires org.apache.xmlgraphics.batik.gvt;
  requires batik.shared.resources;
  requires org.apache.xmlgraphics.batik.svggen;
  requires org.apache.xmlgraphics.batik.util;
  requires org.apache.xmlgraphics.batik.constants;
  requires org.apache.xmlgraphics.batik.i18n;
  requires org.apache.xmlgraphics.batik.xml;
  requires xml.apis.ext;
  requires com.formdev.flatlaf;
  requires sierra;
  requires kilo.client;
  requires SteelSeries;
  requires trident;
  requires imgscalr.lib;
  requires com.fazecast.jSerialComm;
  requires vernacular;
  requires hola;
  requires ch.qos.logback.classic;
  requires ch.qos.logback.core;
  requires com.miglayout.core;
  requires com.miglayout.swing;
  requires AbsoluteLayout.RELEASE300;
}
