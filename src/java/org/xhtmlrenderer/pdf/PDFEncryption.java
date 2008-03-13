/*
 * {{{ header & license
 * Copyright (c) 2007 Jason Blumenkrantz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.pdf;

import com.lowagie.text.pdf.PdfWriter;

public class PDFEncryption {
    private byte[] _userPassword;
    private byte[] _ownerPassword;
    private int _allowedPrivileges = PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_FILL_IN;
    
    public PDFEncryption() {
    }

    public PDFEncryption(byte[] userPassword, byte[] ownerPassword) {
        _userPassword = userPassword;
        _ownerPassword = ownerPassword;
    }

    public PDFEncryption(byte[] userPassword, byte[] ownerPassword, int allowedPrivileges) {
        _userPassword = userPassword;
        _ownerPassword = ownerPassword;
        _allowedPrivileges = allowedPrivileges;
    }

    public byte[] getUserPassword() {
        return _userPassword;
    }

    public void setUserPassword(byte[] userPassword) {
        _userPassword = userPassword;
    }

    public byte[] getOwnerPassword() {
        return _ownerPassword;
    }

    public void setOwnerPassword(byte[] ownerPassword) {
        _ownerPassword = ownerPassword;
    }

    public int getAllowedPrivileges() {
        return _allowedPrivileges;
    }

    public void setAllowedPrivileges(int allowedPrivileges) {
        _allowedPrivileges = allowedPrivileges;
    }
}



