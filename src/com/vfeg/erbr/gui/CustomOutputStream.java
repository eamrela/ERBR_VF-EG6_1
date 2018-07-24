/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vfeg.erbr.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

/**
 *
 * @author eelaamr
 */
public class CustomOutputStream extends OutputStream {
    private JTextArea textArea;
     
    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }
     
    @Override
    public void write(int b) throws IOException {
        try {
            // redirects data to the text area
            textArea.getDocument().remove(0, textArea.getDocument().getLength()-20000);
        } catch (BadLocationException ex) {
            Logger.getLogger(CustomOutputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        textArea.append(String.valueOf((char)b));
        
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    
}
