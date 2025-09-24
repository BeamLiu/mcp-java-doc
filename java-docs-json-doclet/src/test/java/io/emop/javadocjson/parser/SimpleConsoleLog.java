package io.emop.javadocjson.parser;

import org.apache.maven.plugin.logging.Log;

/**
 * 简单的控制台日志实现
 */
public class SimpleConsoleLog implements Log {
    @Override
    public boolean isDebugEnabled() { return true; }

    @Override
    public void debug(CharSequence content) {
        System.out.println("[DEBUG] " + content);
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
        System.out.println("[DEBUG] " + content);
        if (error != null) error.printStackTrace();
    }

    @Override
    public void debug(Throwable error) {
        System.out.println("[DEBUG] ");
        if (error != null) error.printStackTrace();
    }

    @Override
    public boolean isInfoEnabled() { return true; }

    @Override
    public void info(CharSequence content) {
        System.out.println("[INFO] " + content);
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        System.out.println("[INFO] " + content);
        if (error != null) error.printStackTrace();
    }

    @Override
    public void info(Throwable error) {
        System.out.println("[INFO] ");
        if (error != null) error.printStackTrace();
    }

    @Override
    public boolean isWarnEnabled() { return true; }

    @Override
    public void warn(CharSequence content) {
        System.out.println("[WARN] " + content);
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
        System.out.println("[WARN] " + content);
        if (error != null) error.printStackTrace();
    }

    @Override
    public void warn(Throwable error) {
        System.out.println("[WARN] ");
        if (error != null) error.printStackTrace();
    }

    @Override
    public boolean isErrorEnabled() { return true; }

    @Override
    public void error(CharSequence content) {
        System.err.println("[ERROR] " + content);
    }

    @Override
    public void error(CharSequence content, Throwable error) {
        System.err.println("[ERROR] " + content);
        if (error != null) error.printStackTrace();
    }

    @Override
    public void error(Throwable error) {
        System.err.println("[ERROR] ");
        if (error != null) error.printStackTrace();
    }
}