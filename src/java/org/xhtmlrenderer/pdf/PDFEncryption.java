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
import org.xhtmlrenderer.util.ArrayUtil;

public class PDFEncryption {
    private byte[] _userPassword;
    private byte[] _ownerPassword;
    private int _allowedPrivileges = PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_FILL_IN;
    private int _encryptionType = PdfWriter.STANDARD_ENCRYPTION_128;
    
    public PDFEncryption() {
    }

    public PDFEncryption(byte[] userPassword, byte[] ownerPassword) {
        _userPassword = ArrayUtil.cloneOrEmpty(userPassword);
        _ownerPassword = ArrayUtil.cloneOrEmpty(ownerPassword);
    }

    public PDFEncryption(byte[] userPassword, byte[] ownerPassword, int allowedPrivileges) {
        _userPassword = ArrayUtil.cloneOrEmpty(userPassword);
        _ownerPassword = ArrayUtil.cloneOrEmpty(ownerPassword);
        _allowedPrivileges = allowedPrivileges;
    }
    
    public PDFEncryption(byte[] userPassword, byte[] ownerPassword, int allowedPrivileges, int encryptionType) {
        _userPassword = ArrayUtil.cloneOrEmpty(userPassword);
        _ownerPassword = ArrayUtil.cloneOrEmpty(ownerPassword);
        _allowedPrivileges = allowedPrivileges;
        _encryptionType = encryptionType;
    }

    public byte[] getUserPassword() {
        return ArrayUtil.cloneOrEmpty(_userPassword);
    }

    public void setUserPassword(byte[] userPassword) {
        _userPassword = ArrayUtil.cloneOrEmpty(userPassword);
    }

    public byte[] getOwnerPassword() {
        return ArrayUtil.cloneOrEmpty(_ownerPassword);
    }

    public void setOwnerPassword(byte[] ownerPassword) {
        _ownerPassword = ArrayUtil.cloneOrEmpty(ownerPassword);
    }

    public int getAllowedPrivileges() {
        return _allowedPrivileges;
    }
        
    public void setAllowedPrivileges(int allowedPrivileges) {
        _allowedPrivileges = allowedPrivileges;
    }
    
    public int getEncryptionType() {
        return _encryptionType;
    }

    public void setEncryptionType(int encryptionType ) {
        _encryptionType  = encryptionType;
    }
    
}



