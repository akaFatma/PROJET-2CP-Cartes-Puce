package jsr268gp.sampleapplet;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class SampleTestApplet extends Applet {

    final short SW_VERIFICATION_FAILED = 0x6300;
    final short SW_PIN_VERIFICATION_REQUIRED = 0x6301;

    RSAPrivateKey cardPrivateKey;
    OwnerPIN PIN;
    byte[] ID = new byte[4];//ID fixe a 4 bytes

    byte[] n = new byte[128];
    byte[] N = new byte[128];
    byte[] A = new byte[128];
    RSAPublicKey clientPublicKey;
    AESKey sessionKey;

    MessageDigest digest;
    RandomData random;
    Cipher cipher;

    byte[] generatedArray;
    byte[] array;
    byte[] array2;
    byte[] hashArray2;
    byte[] hashArray = new byte[32];
    short offset = 0;

    private SampleTestApplet() {
        random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        digest = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
    	//Cle publique du serveur
        clientPublicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KeyBuilder.LENGTH_RSA_1024,false);
        //Cle de session
        sessionKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        //PIN avec nombre d'essais max fixe a 3 et taille fixe a 2 bytes
        PIN = new OwnerPIN((byte) 0x03, (byte) 0x02);
        //Cle privee de la carte
        cardPrivateKey = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, KeyBuilder.LENGTH_RSA_1024,false);
    }

    public static void install(byte bArray[], short bOffset, byte bLength)
            throws ISOException {
        new SampleTestApplet().register();
    }

    public void process(APDU apdu) throws ISOException {
        if (selectingApplet())
            return;
        byte[] buffer = apdu.getBuffer();
        short Len_DataOut = (short) buffer[ISO7816.OFFSET_P2];
        if (Len_DataOut < 0) // la carte comprend 0x80 ie 128 comme -1 
            Len_DataOut = (short) 128;
        short Len_DataIn;

        generatedArray = JCSystem.makeTransientByteArray(Len_DataOut,
                JCSystem.CLEAR_ON_DESELECT);

        switch (buffer[ISO7816.OFFSET_P1]) {
            case (byte) 0x01: //Phase de personnalisation
                switch (buffer[ISO7816.OFFSET_INS]) {
                    case (byte) 0x01:// INS_MODULUS:
                    	//Recuperer le modulo du buffer et faire setModulus a la cle privee
                        Len_DataIn = apdu.setIncomingAndReceive();
                        array = JCSystem.makeTransientByteArray((short) 128,JCSystem.CLEAR_ON_DESELECT);
                        getData(array, buffer, ISO7816.OFFSET_CDATA, Len_DataIn);
                        cardPrivateKey.setModulus(array, offset, Len_DataIn);
                        break;
                    case (byte) 0x02:// INS_PRIVATE_EXPONENT:
                    	//Recuperer l'exposant du buffer et faire setExponent a la cle privee
                        Len_DataIn = apdu.setIncomingAndReceive();
                        array = JCSystem.makeTransientByteArray((short) 128,JCSystem.CLEAR_ON_DESELECT);
                        getData(array, buffer, ISO7816.OFFSET_CDATA, Len_DataIn);
                        cardPrivateKey.setExponent(array, offset, Len_DataIn);
                        break;
                    case (byte) 0x03:// INS_SET_PIN:
                    	//Recuperer le PIN du buffer et update sa valuer dans l'objet de la classe OwnerPIN
                        Len_DataIn = apdu.setIncomingAndReceive();
                        array = JCSystem.makeTransientByteArray(Len_DataIn,JCSystem.CLEAR_ON_DESELECT);
                        getData(array, buffer, ISO7816.OFFSET_CDATA, Len_DataIn);
                        PIN.update(array, offset, (byte) Len_DataIn);
                        break;
                    case (byte) 0x04:// INS_SET_ID:
                    	//Recuperer le ID du buffer
                        Len_DataIn = apdu.setIncomingAndReceive();
                        getData(ID, buffer, ISO7816.OFFSET_CDATA, Len_DataIn);
                        break;
                }
                Util.arrayCopyNonAtomic(generatedArray, offset, buffer, offset,
                        Len_DataOut);
                apdu.setOutgoingAndSend(offset, Len_DataOut);
                break;
            case (byte) 0x02:
                switch (buffer[ISO7816.OFFSET_INS]) {
                    case (byte) 0x01:// INS_MODULUS:
                    	//Recuperer le modulo du buffer et faire setModulus a la cle publique de serveur
                        Len_DataIn = apdu.setIncomingAndReceive();
                        array = JCSystem.makeTransientByteArray((short) 128,JCSystem.CLEAR_ON_DESELECT);
                        getData(array, buffer, ISO7816.OFFSET_CDATA, Len_DataIn);
                        clientPublicKey.setModulus(array, offset, Len_DataIn);
                        break;
                    case (byte) 0x02:// INS_PUBLIC_EXPONENT:
                    	//Recuperer l'exposant du buffer et faire setExponent a la cle publique de serveur
                        Len_DataIn = apdu.setIncomingAndReceive();
                        array = JCSystem.makeTransientByteArray(Len_DataIn,JCSystem.CLEAR_ON_DESELECT);
                        getData(array, buffer, ISO7816.OFFSET_CDATA, Len_DataIn);
                        clientPublicKey.setExponent(array, offset, Len_DataIn);
                        break;
                    case (byte) 0x03:// INS_N:
                    	//Recuperer N du buffer
                        Len_DataIn = apdu.setIncomingAndReceive();
                        getData(N, buffer, ISO7816.OFFSET_CDATA, Len_DataIn);
                        break;
                    case (byte) 0x04:// INS_A:
                    	//Recuperer A du buffer
                        Len_DataIn = apdu.setIncomingAndReceive();
                        getData(A, buffer, ISO7816.OFFSET_CDATA, Len_DataIn);
                        break;
                    case (byte) 0x05: // INS recuperer AmmodN et envoyer AnmodN
                    	
                    	//Recuperer AmmodN du buffer
                        Len_DataIn = apdu.setIncomingAndReceive();
                        array = JCSystem.makeTransientByteArray(Len_DataIn,JCSystem.CLEAR_ON_DESELECT);
                        getData(array, buffer, ISO7816.OFFSET_CDATA, Len_DataIn);
                        
                        //Generer le nombre privee n
                        n = Generer_n(N);
                        
                        //Calcule de la cle de session
                        sessionKey.setKey(Mask(digest, RSA_mod_pow(N, array, n), offset,(short) 128, (short) 16),offset);
                        
                        //Hashage de la concatenation AmmodN||AnmodN
                        hash(concatenateArrays(array, RSA_mod_pow(N, A, n)), hashArray);
                        
                        //Envoi de AnmodN
                        getData(generatedArray, RSA_mod_pow(N, A, n), offset,Len_DataOut);
                        
                        break;
                    case (byte) 0x06:// INS_SIGN
                    	//Calcul et envoie de la signature de la carte 
                        array2 = JCSystem.makeTransientByteArray((short) 128,JCSystem.CLEAR_ON_DESELECT);
                        sign();
                        getData(generatedArray, array2, offset, Len_DataOut);
                        break;
                    case (byte) 0x07:// INS_VERIF:
                    	//Recuperer du buffer la signature du serveur et la verification
                        Len_DataIn = apdu.setIncomingAndReceive();
                        array = JCSystem.makeTransientByteArray((short) 128,JCSystem.CLEAR_ON_DESELECT);
                        getData(array, buffer, ISO7816.OFFSET_CDATA, Len_DataIn);
                        verify();
                        break;
                }
                Util.arrayCopyNonAtomic(generatedArray, offset, buffer, offset,
                        Len_DataOut);
                apdu.setOutgoingAndSend(offset, Len_DataOut);
                break;
            case (byte) 0x03: // VOTE:
                switch (buffer[ISO7816.OFFSET_INS]) {
                    case (byte) 0x01: // INS_VERIFY_PIN:
                    	//Verification de la validite du PIN
                        if (PIN.isValidated())return; //PIN deja validé
                    
                    	//Recuperer le PIN du buffer
                        Len_DataIn = apdu.setIncomingAndReceive();
                        array = JCSystem.makeTransientByteArray(Len_DataIn,JCSystem.CLEAR_ON_DESELECT);
                        getData(array, buffer, ISO7816.OFFSET_CDATA, Len_DataIn);
                        
                        if (!PIN.check(array, offset, (byte) Len_DataIn)) {//Compare le pin en entree avec le PIN de l'objet OwnerPIN
                            if (PIN.getTriesRemaining() == 0) {//Le nombre d'essai disponible
                                ISOException.throwIt(SW_VERIFICATION_FAILED);//carte bloquee
                            } else {
                                ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);//pin errone
                            }
                        }
                        break;
                    case (byte) 0x02: // INS_ID:
                    	//Envoie de l'ID de la carte
                        getData(generatedArray, ID, offset, Len_DataOut);
                        break;
                    case (byte) 0x03: //INS_SESSION_KEY:
                    	//Envoie de la cle de session
                    	sessionKey.getKey(generatedArray, offset);
                    	break;
                }
                Util.arrayCopyNonAtomic(generatedArray, offset, buffer, offset,
                        Len_DataOut);
                apdu.setOutgoingAndSend(offset, Len_DataOut);
                break;
        }
    }

    //Méthode qui copie de In dans Out
    public void getData(byte[] Out, byte[] In, short offset, short Len_DataOut) {
        for (short i = 0; i < Len_DataOut; i++) {
            Out[i] = In[(short) (offset + i)];
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------------------
  
    //Méthode qui compare deux tableaux
    public static short compareArrays(byte[] array1, byte[] array2) {
        // Vérifier si les deux tableaux ont la même longueur
        if (array1.length != array2.length) {
            // Retourner -1 si le premier tableau est plus grand, 1 sinon
            return (short) ((array1.length > array2.length) ? -1 : 1);
        }

        // Parcourir les deux tableaux et comparer chaque élément
        for (short i = 0; i < array1.length; i++) {
            // Comparer les éléments des deux tableaux
            if (array1[i] != array2[i]) {
                // Retourner -1 si le premier élément est plus grand, 1 sinon
                return (short) ((array1[i] > array2[i]) ? -1 : 1);
            }
        }

        // Si tous les éléments sont identiques, retourner 0
        return 0;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------------

    //Méthode qui genere un nombre aleatoire n inferieur a N
    public byte[] Generer_n(byte[] N) {
        byte[] randomData = JCSystem.makeTransientByteArray((short) 128,
                JCSystem.CLEAR_ON_DESELECT); // declarer tableau
        do {
            random.generateData(randomData, (short) 0,
                    (short) randomData.length);
        } while ((compareArrays(randomData, N)) >= 0); // Répéter jusqu'à ce que
        // n soit inférieur à N
        return randomData;
    }

    //Methode qui calcul l'exponnentiel avec une cle RSA 
    public byte[] RSA_mod_pow(byte[] N, byte[] A, byte[] n) {
        short NLength = (short) N.length;
        short ALength = (short) A.length;
        short nLength = (short) n.length;

        // initialize le buffer de resultat
        byte[] result = JCSystem.makeTransientByteArray((short) 128,JCSystem.CLEAR_ON_DESELECT);

        // set l'exposant et le modulo
        RSAPrivateKey modPow = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, KeyBuilder.LENGTH_RSA_1024, false);
        modPow.setModulus(N, offset, NLength);
        modPow.setExponent(n, offset, nLength);

        // initialize le cipher au dechiffrement
        cipher = Cipher.getInstance(Cipher.ALG_RSA_NOPAD, false);
        cipher.init(modPow, Cipher.MODE_DECRYPT);

        // calcul l'exponnentiel
        cipher.doFinal(A, offset, ALength, result, offset);

        return result;
    }

    //Methode du masque
    public static byte[] Mask(MessageDigest digest, byte[] mgfSeed,
            short mgfSeedOffset, short mgfSeedLength, short maskLen) {
        short hashLength = (short) digest.getLength();
        short hashCount = (short) ((short) (maskLen + hashLength - 1) / hashLength);
        short maskIndex = 0;

        byte[] tempBuffer = new byte[hashLength];
        byte[] mask = new byte[maskLen];

        for (short i = 0; i < hashCount; i++) {
            digest.reset();
            digest.update(mgfSeed, mgfSeedOffset, mgfSeedLength);
            digest.doFinal(new byte[] { 0x00, 0x00, 0x00, (byte) i },
                    (short) 0, (short) 4, tempBuffer, (short) 0);

            short m = (short) (maskLen - maskIndex);
            short lengthToCopy = hashLength > m ? m : hashLength;
            for (short j = 0; j < lengthToCopy; j++) {
                mask[(short) (maskIndex + j)] = tempBuffer[j];
            }
            maskIndex += hashLength;
        }

        byte[] output = new byte[maskLen];
        for (short i = 0; i < maskLen; i++) {
            output[i] = mask[i];
        }
        return output;
    }
    
    //Methode qui concatene deux tableaux
    public static byte[] concatenateArrays(byte[] array1, byte[] array2) {
        byte[] concatenated = new byte[(short) (array1.length + array2.length)];
        for (short i = 0; i < (short) array1.length; i++) {
            concatenated[i] = array1[i];
        }

        for (short i = 0; i < (short) array2.length; i++) {
            concatenated[(short) (array1.length + i)] = array2[i];
        }

        return concatenated;
    }

    
    //Methode qui fait le chiffrement/dechiffrement symetrique AES d'une donnée en utilisant la cle de session
    public void Sym(byte Mode, byte[] In, byte[] Out) {
        cipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        byte[] iv = JCSystem.makeTransientByteArray((short) 16, JCSystem.CLEAR_ON_DESELECT);
        sessionKey.getKey(iv, offset);
        cipher.init(sessionKey, Mode, iv, offset, (short) iv.length);
        cipher.doFinal(In, offset, (short) In.length, Out, offset);
    }

    
    //Methode qui fait le chiffrement/dechiffrement asymetrique RSA d'une donnée en utilisant une cle RSA
    public void Asym(Key key, byte Mode, byte[] In, byte[] Out) {
        cipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);
        cipher.init(key, Mode);
        cipher.doFinal(In, offset, (short) In.length, Out, offset);
    }

    //Methode qui calcul la signature de la carte
    public void sign() {
        array = JCSystem.makeTransientByteArray((short) 128,JCSystem.CLEAR_ON_DESELECT);
        //chiffrement asymetrique du tableau hashé avec la cle privee de la carte
        Asym(cardPrivateKey, Cipher.MODE_ENCRYPT,hashArray, array);
        //chiffrement symetrique de la signature avec la cle de session
        Sym(Cipher.MODE_ENCRYPT, array, array2);
    }

    //Methode qui verifie la signature du serveur 
    public void verify() {
        array2 = JCSystem.makeTransientByteArray((short) 128, JCSystem.CLEAR_ON_DESELECT);
        hashArray2 = JCSystem.makeTransientByteArray((short) 32, JCSystem.CLEAR_ON_DESELECT);
        //dechiffrement symetrique de la signature avec la cle de session
        Sym(Cipher.MODE_DECRYPT, array, array2);
        //dechiffrement symetrique de la signature avec la cle publique du serveur
        Asym(clientPublicKey, Cipher.MODE_DECRYPT, array2, hashArray2);
        //comparaison entre le tableau de hashage resultant des dechiffrement de la signature et celui calcule auparavant
        generatedArray[0] = (compareArrays(hashArray, hashArray2) == (short) 0 ? (byte) 0x02: (byte) 0x04);//retourne 0x02 si egaux et 0x04 sinon
    }

    //Methode qui fait le hashage d'un tableau avec SHA-256
    public void hash(byte[] In, byte[] Out) {
        digest.reset();
        digest.doFinal(In, offset, (short) In.length, Out, offset);
    }

}