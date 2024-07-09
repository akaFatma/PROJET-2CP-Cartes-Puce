import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.sql.*;
import java.util.*;
import java.security.interfaces.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import util.Util;

public class Server {
	
	    private static final String DB_URL = "jdbc:mysql://localhost:3306/voting_app";
	    private static final String DB_USER = "root";
	    private static final String DB_PASSWORD = "root";
	    
	    private static KeyPair keyPair;
	    private static RSAPublicKey cardPublicKey;
	    private static RSAPublicKey serverPublicKey;
	    private static RSAPrivateKey serverPrivateKey;
	    
	    static String id;
		static String password;
		static byte[] N_array = new byte[128];
		static byte[] A_array = new byte[128];
		static byte[] AmmodN_array = new byte[128];
		static byte[] concat = new byte[256];
		static byte[] chiffree = new byte[128];
		static byte[] CLE = new byte[128];
		static byte[] ID = new byte[4];
		static byte[] signature;
		static BigInteger m, N;

	private static Connection connection;

	static {
		//Connexion a la bdd mysql
		try {
			String url = "jdbc:mysql://localhost:3306/voting_app";
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(url, DB_USER,DB_PASSWORD);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/********************************************************************************************************************************************************************** */
	//Methode qui permet de creer un nouveau voteur a partir de son nom, prenom et cle publique
	public static boolean  createNewVoter(String firstName, String lastName,String publicKey) {
		boolean success = false;
		try {
			String queryLastId = "SELECT MAX(voter_id) FROM VOTERS";
			PreparedStatement statementLastId = connection.prepareStatement(queryLastId);
			ResultSet rs = statementLastId.executeQuery();
			int lastVoterId = 0;
			if (rs.next()) {
				lastVoterId = rs.getInt(1) ;
			}
			int voterId = lastVoterId+1;
			String query = "INSERT INTO VOTERS (voter_id, first_name, last_name, public_key) VALUES (?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, voterId);
			statement.setString(2, firstName);
			statement.setString(3, lastName);
			statement.setString(4, publicKey);

			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				System.out.println("New voter created successfully.");
				success = true;
			} else {
				System.out.println("Failed to create new voter.");
			}

			statement.close();
		} catch (Exception e) {
			System.out.println("Error creating new voter: " + e.getMessage());
		}
		return success;
	}

	/********************************************************************************************************************************************************* */
	//Methode qui retourne la liste des nominees 
	public static ArrayList<Nominee> nominees() {

		ArrayList<Nominee> nomineeList = new ArrayList<>();
		try {

			String querry1 = "SELECT * FROM nominees";
			java.sql.Statement st = connection.createStatement();
			ResultSet res = st.executeQuery(querry1);
			Nominee nom;

			while (res.next()) {
				nom = new Nominee(res.getInt("nominee_id"), res.getString("nominee_name"));
				nomineeList.add(nom);

			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return nomineeList;

	}

	/********************************************************************************************************************************************************* */
	//Methode qui retourne le prochain ID a ajouter dans le tableau des voteurs
	public static String getNewId() {
		int lastVoterId = 0;
		try {
			String url = "jdbc:mysql://localhost:3306/voting_app";
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
			
			String query = "SELECT voter_id FROM voters ORDER BY voter_id DESC LIMIT 1";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				lastVoterId = resultSet.getInt("voter_id") + 1;
			}

		} catch (Exception e) {
			System.out.print(e);
		}
		System.out.println(lastVoterId);
		return Integer.toString(lastVoterId);

	}


	/***************************************************************************************************/
// Methode qui retourne la cle publique a partir d'un ID
	public static RSAPublicKey getPublicKeyById(int voter_id) {
		RSAPublicKey publicKey = null;
		try {
			String querry = "SELECT public_key FROM voters WHERE voter_id = ?";
			PreparedStatement statement = connection.prepareStatement(querry);
			statement.setInt(1, voter_id);
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
						Util.hexStringToByteArray(resultSet.getString("public_key")));
				publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return publicKey;
	}

	/**************************************************************************************************/

	//Methode qui verifie si la personne a deja vote ou pas
	public static boolean hasVoted(int voter_id) {
		try {
			String querry = "SELECT nominee_id FROM voters WHERE voter_id = ?";
			PreparedStatement statement = connection.prepareStatement(querry);
			statement.setInt(1, voter_id);
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {//en verifiant si le champs nominee_id a ete modifie ou pas 
				if(resultSet.getString("nominee_id") != null)return true;
				else  return false;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return true;
	}

	/**************************************************************************************************/
	//Methode qui calcule le score de chaque nominee
	public static HashMap<String, String> calculScores(){
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
	             PreparedStatement statement = connection.prepareStatement("SELECT nominee_id, session_key FROM voters");
	             ResultSet resultSet = statement.executeQuery()) {
			ArrayList<Nominee> nominees = nominees();
			HashMap<String, String> scoreMap = convertToMap(nominees);
	            while (resultSet.next()) {
	            	//Si cette personne a vote, les champs session_key et nominee_id seront different de null
	            	if(resultSet.getString("session_key") != null && resultSet.getString("nominee_id")!= null ){
	                
	            	//recuperation et dechiffrement de la cle de session avec la cle privee du serveur
	            	byte[] sessionKey = decrypt(Util.hexStringToByteArray(resultSet.getString("session_key")), serverPrivateKey);
	                
	            	//recuperation et dechiffrement du choix avec la cle de session
	            	byte[] choice = Util.hexStringToByteArray(resultSet.getString("nominee_id"));
	                int id = Util.byteArrayToUnsignedInt(decrypt_aes(choice, sessionKey, "AES/CBC/PKCS5Padding"));
	                
	                //Pour chaque nominee, verifier si le choix du voteur est egal a son nom 
	                for (Nominee nominee : nominees) {
	                    if (Integer.toString(nominee.getNomineeId()).equals(Integer.toString(id))) {
	                        String nomineeName = nominee.getName();

	                        //incrementer le score du nominee dans la map 
	                        String score = scoreMap.getOrDefault(nomineeName, "0");
	                        scoreMap.put(nomineeName, String.valueOf(Integer.parseInt(score) + 1));

	                        System.out.println("Score incrementee pour nominee: " + nomineeName);
	                        break;
	                    }
	                }}}
	            return scoreMap;
	            }catch(Exception e){
		System.out.println(e.getMessage());
		}
		 return null;
		}
	
	/**************************************************************************************************/
	
	//Methode qui permet la communication avec le client
	public static void startServer() {
		try {
			//Demarage du serveur
			ServerSocket serverSocket = new ServerSocket(5001);
			System.out.println("Listening for clients...");
			while (true) {
				Socket clientSocket = serverSocket.accept();
				
				try {
					//Connexion au client
					String clientSocketIP = clientSocket.getInetAddress().toString();
					int clientSocketPort = clientSocket.getPort();
					System.out.println("[IP: " + clientSocketIP + ", Port: " + clientSocketPort + "]  "
							+ "Client Connection Successful!");

					DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
					DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());
					
					StringBuilder serverMessageBuilder;
					String serverMessage,clientMessage;
					ArrayList<Nominee> nomineeList;
					boolean quit= false;
					
					do{
						clientMessage = dataIn.readUTF();
						System.out.println("Received request from client: " + clientMessage);
						switch (clientMessage) {

						case "GET_NEW_ID"://Retourner le ID de la nouvelle carte 
							dataOut.writeUTF(getNewId());
							dataOut.flush();
							break;

						case "CREATE_NEW_VOTER"://Creer un nouveau voteur avec sa cle publique, son nom et son prenom
							String key = dataIn.readUTF();
							String firstname= dataIn.readUTF();
							String lastname= dataIn.readUTF();
							if(createNewVoter(firstname, lastname,key)) dataOut.writeUTF("SUCCESS");
							
							break;

						case "SHOW_ALL_NOMINEES"://Envoyer la liste des nominees (Nom, ID) chiffre
							nomineeList = nominees();
							serverMessageBuilder = new StringBuilder();

							for (Nominee nominee : nomineeList) {
								String NomineeInfo = "Nominee ID : " + nominee.getNomineeId() + ",Name: "
										+ nominee.getName();
								serverMessageBuilder.append(NomineeInfo).append("\n");
							}
							serverMessage = serverMessageBuilder.toString();
							
							//Chiffrer la liste de nominees avec la cle de session, pour garder les IDs privees
							String encrypted = Util.byteArrayToHexString(encrypt_aes(Util.hexStringToByteArray(Util.ASCIIToHex(serverMessage)), CLE, "AES/CBC/PKCS5Padding"), "");
							dataOut.writeUTF(encrypted);
							break;

						case "RECEIVE_N_A"://Recevoir les tableau N et A 
							dataIn.read(N_array);
							System.out.println("N: " + Util.byteArrayToHexString(N_array, " "));
							dataIn.read(A_array);
							System.out.println("A: " + Util.byteArrayToHexString(A_array, " "));
							break;

						case "GET_PUBLIC_KEY"://Envoyer la cle publique de serveur
							dataOut.writeUTF(Util.byteArrayToHexString(serverPublicKey.getEncoded(),""));
							dataOut.flush();
							break;

						case "RECEIVE_ID"://Recevoir le ID de la carte pour recuperer sa cle publique 
							dataIn.read(ID);
							cardPublicKey = getPublicKeyById(Util.byteArrayToUnsignedInt(ID));
						
							break;

						case "A_VOTE"://Retourner si le proprietaire de cette carte a deja voté ou pas a partir de son ID
							dataIn.read(ID);
							if(hasVoted(Util.byteArrayToUnsignedInt(ID))){
								dataOut.writeUTF("A_VOTE");System.out.println("hna a vote");
								dataOut.flush();
							}
							else{
								dataOut.writeUTF("PAS_VOTE");System.out.println("hna pas vote");
								dataOut.flush();
								}
							break;

						case "GET_AmmodN"://Calcul de A^m mod N 
							N = new BigInteger(1, N_array);
							BigInteger A = new BigInteger(1, A_array);
							
							//calcul du nombre privee du serveur
							m = Generer_m(N);

							//calcul de l'exponnentiel 
							BigInteger AmmodN = A.modPow(m, N);
							AmmodN_array = Util.toByteArray(AmmodN);

							dataOut.write(AmmodN_array);
							dataOut.flush();
							break;

						case "RECEIVE_AnmodN"://Reception de A^n mod N et calcul de la cle de session
							byte[] AnmodN_array = new byte[128];
							dataIn.read(AnmodN_array);
							System.out.println("AnmodN" + Util.byteArrayToHexString(AnmodN_array, " "));
							BigInteger AnmodN = new BigInteger(1, AnmodN_array);
							
							//Calcul de la cle de session 
							BigInteger AmnmodN = AnmodN.modPow(m, N);
							byte[] AmnmodN_array = Util.toByteArray(AmnmodN);
							MessageDigest digest = MessageDigest.getInstance("SHA-256");
							CLE = Mask(digest, AmnmodN_array, 0, AmnmodN_array.length, 16);
							
							//Concatenation A^m mod N || A^n mod N pour plus tard
							concat = Util.concatenateArrays(AmmodN_array, AnmodN_array);
							break;

						case "RECEIVE_sign"://Recevoir la signature de la carte 
							dataIn.read(chiffree);
							System.out.println("Signature carte" + Util.byteArrayToHexString(chiffree, " "));
							
							//dechiffrement symetrique par la cle de session
							signature = decrypt_aes(chiffree, CLE, "AES/CBC/NoPadding");
							
							//Verification de la signature
							boolean verif = verify(concat, signature, cardPublicKey);
							System.out.println("Verification : " + verif);
							
							//Envoie du resultat de la verification 
							if(verif) dataOut.writeUTF("SUCCESS");
							else dataOut.writeUTF("FAIL");
							
							break;
						case "GET_sign"://Envoyer la signature du serveur
							//calcul de la signature
							signature = sign(concat, serverPrivateKey);
							
							//chiffrement symetrique avec la cle de session
							chiffree = encrypt_aes(signature, CLE, "AES/CBC/NoPadding");
							System.out.println("Signature serveur : " + Util.byteArrayToHexString(chiffree, " "));
							
							//Envoi de la signature
							dataOut.write(chiffree);
							dataOut.flush();
							break;
							
						case "VOTE"://Recevoir le choix du voteur
                             byte[] choix = Util.hexStringToByteArray(dataIn.readUTF());
                             
                             //chiffrement de la cle de session avec la cle publique du serveur
                             byte[] sessionKey = encrypt(CLE, serverPublicKey);
                             System.out.println("choix : "+Util.byteArrayToHexString(choix, ""));
                             System.out.println("session : "+Util.byteArrayToHexString(sessionKey, ""));
                             System.out.println("ID : "+Util.byteArrayToHexString(ID, ""));
                             
                             //enregistrement du vote
                             boolean success = updateChoice(Util.byteArrayToUnsignedInt(ID),Util.byteArrayToHexString(choix, ""), Util.byteArrayToHexString(sessionKey, ""));
                             if (success) {
                                 dataOut.writeUTF("SUCCESS");
                                 System.out.println("Vote enregistre");
                             } else {
                                 dataOut.writeUTF("FAIL");
                                 System.out.println("Erreur a l'enregistrement");
                             }
                             break;
							
						case "LOGIN_ADMIN" ://authentification de l'admin
							//Recuperer le ID et le mot de passe haché en entree
							id = dataIn.readUTF();
                            System.out.println("Received ID: " + id);
                            password = dataIn.readUTF();
                            System.out.println("Received password: " + password);
                            
                            //Authentification de l'admin
                            if(loginAdmin(id, password)) dataOut.writeUTF("SUCCESS");
                            else dataOut.writeUTF("FAIL");
                           break;
						case "LOGIN_RESPO" ://authentification du responsable du vote
							//Recuperer le ID et le mot de passe haché en entree
							id = dataIn.readUTF();
                            System.out.println("Received ID: " + id);
                            password = dataIn.readUTF();
                            System.out.println("Received password: " + password);
                            
                            //Authentification du respo 
                            if(loginRespo(id, password)) dataOut.writeUTF("SUCCESS");
                            else dataOut.writeUTF("FAIL");
                           break;
						case "SHOW_STAT"://Envoyer les statistique(Nominee name + score) 
							HashMap<String, String> statMap = calculScores();
							serverMessageBuilder = new StringBuilder();

							for (HashMap.Entry<String, String> entry : statMap.entrySet()) {
								String NomineeInfo = "Nominee score : " + entry.getValue() + ",Name: "
										+ entry.getKey();
								serverMessageBuilder.append(NomineeInfo).append("\n");
							}
							serverMessage = serverMessageBuilder.toString();
							dataOut.writeUTF(serverMessage);
							break;
						case "EXIT":
							quit = true;
							break;
						}
					}while(!quit);
					dataIn.close();
					dataOut.close();
					clientSocket.close();
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**************************************************************************************************/
	//Methode de generation du nombre aleatoire privee du serveur 
	public static BigInteger Generer_m(BigInteger N){
		SecureRandom random = new SecureRandom();
		BigInteger m;
		do {
			m = new BigInteger(1024, random);

		} while (m.compareTo(N) >= 0);
		return m;
	}
	
	/**************************************************************************************************/

	//Methode de calcul du masque 
	public static byte[] Mask(MessageDigest digest, byte[] mgfSeed, int mgfSeedOffset, int mgfSeedLength,
			int maskLen) throws DigestException {
		int hashLength = digest.getDigestLength();
		int hashCount = (maskLen + hashLength - 1) / hashLength;
		int maskIndex = 0;

		byte[] tempBuffer = new byte[hashLength];
		byte[] mask = new byte[maskLen];

		for (int i = 0; i < hashCount; i++) {
			digest.reset();
			digest.update(mgfSeed, mgfSeedOffset, mgfSeedLength);
			digest.update((byte) (i >>> 24));
			digest.update((byte) (i >>> 16));
			digest.update((byte) (i >>> 8));
			digest.update((byte) i);
			digest.digest(tempBuffer, 0, hashLength);

			int bytesToCopy = Math.min(maskLen - maskIndex, hashLength);
			System.arraycopy(tempBuffer, 0, mask, maskIndex, bytesToCopy);
			maskIndex += bytesToCopy;
		}

		return mask;
	}



	/**************************************************************************************************/

	//Methode de dechiffrement aes cbc soit PKCS5 ou NOPADDING selon le choix du Mode
	public static byte[] decrypt_aes(byte[] encryptedText, byte[] key, String Mode) throws Exception {
		SecretKey originalKey = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance(Mode);
		cipher.init(Cipher.DECRYPT_MODE, originalKey, new IvParameterSpec(key));//IV aleatoire
		byte[] decryptedBytes = cipher.doFinal(encryptedText, 0, encryptedText.length);
		return decryptedBytes;
	}

	/**************************************************************************************************/
	
	//Methode de chiffrement aes cbc soit PKCS5 ou NOPADDING selon le choix du Mode
	public static byte[] encrypt_aes(byte[] data, byte[] key, String Mode) throws Exception {
		SecretKey originalKey = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance(Mode);
		cipher.init(Cipher.ENCRYPT_MODE, originalKey, new IvParameterSpec(key));//IV aleatoire
		byte[] encryptedBytes = cipher.doFinal(data, 0, data.length);
		return encryptedBytes;

	}

	/**************************************************************************************************/

	//Methode de verification de la signature de la carte
	public static boolean verify(byte[] message, byte[] signature, RSAPublicKey publicKey) throws Exception {
		//dechiffrement symetrique avec la cle publique de la carte
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		byte[] result = cipher.doFinal(signature, 0, signature.length);

		//Calcul du hash SHA-256 de la concatenation de A^m mod N || A^n mod N
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		digest.update(message, 0, message.length);
		byte[] result2 = digest.digest();

		//comparaison entre le hash calculé et le resultat du dechiffrement
		return Util.compareArrays(result2, result) == 0 ? true : false;
	}


	/**************************************************************************************************/

	//Methode de calcul de la signature du serveur
	public static byte[] sign(byte[] data, PrivateKey privateKey) {
		try {
			// Calcul du hash SHA-256 de la la concatenation de A^m mod N || A^n mod N
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(data);

			// Chiffrement du hash avec la cle privee du serveur 
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			byte[] signature = cipher.doFinal(hash);

			return signature;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**************************************************************************************************/

	//Methode de chiffrement RSA PKCS1
	public static byte[] encrypt(byte[] data, RSAPublicKey Key ) {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, Key);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**************************************************************************************************/

	//Methode de dechiffrement RSA PKCS1
	public static byte[] decrypt(byte[] data, RSAPrivateKey Key ) {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, Key);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**************************************************************************************************/
	//Methode qui verifie si les cles du serveur ont ete genere
	public static boolean keysExist() throws SQLException {
	        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
	             Statement statement = connection.createStatement()) {
	            String query = "SELECT COUNT(*) FROM server";
	            try (ResultSet resultSet = statement.executeQuery(query)) {
	                resultSet.next();
	                int rowCount = resultSet.getInt(1);
	                return rowCount > 0; 
	            }
	        }
	    }
	// Methode pour recuperer public key and private key du serveur
	 public static void retrieveKeys() throws SQLException, InvalidKeySpecException, NoSuchAlgorithmException {
		    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		         PreparedStatement statement = connection.prepareStatement("SELECT public_key, private_key FROM server WHERE server_id = ?")) {
		        statement.setString(1, "responsable");//ID du serveur

		        try (ResultSet resultSet = statement.executeQuery()) {
		            if (resultSet.next()) {
		                String publicKey = resultSet.getString("public_key");
		                String privateKey = resultSet.getString("private_key");
		                System.out.println("Les cles du serveur ont ete recupere avec succes");
		                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Util.hexStringToByteArray(publicKey));
		                serverPublicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
		                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Util.hexStringToByteArray(privateKey));
		                serverPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
		            } else {
		                System.out.println("No keys found for server_id = responsable");
		            }
		        }
		    }
		}
	 //Methode qui permet de generer une paire de cles RSA pour le serveur
	public static KeyPair generateKey() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			KeyPair keyPair = keyGen.generateKeyPair();
			return keyPair;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	//Methode qui permet d'enregistrer les cles du serveur, ainsi que l'id et le mot de passe du responsable du vote
	public static boolean  saveServerKeys(String privateKey, String publicKey) {
		boolean success = false;
		try {
			String query = "INSERT INTO SERVER (server_id, server_password, private_key, public_key) VALUES ( ?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, "responsable");
			statement.setString(2, "A397FAB237FD36301ACDB2F7DA00A107983E2F0E713A297C68D93C4160F40549");
			statement.setString(3, privateKey);
			statement.setString(4, publicKey);

			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				System.out.println("Success");
				success = true;
			} else {
				System.out.println("Fail");
			}

			statement.close();
		} catch (Exception e) {
			System.out.println("Erreur :"+ e.getMessage());
		}
		return success;
	}
	
	/**************************************************************************************************/
	
	//Methode qui permet de creer la map de nominees a partir d'une liste
	   public static HashMap<String, String> convertToMap(ArrayList<Nominee> nominees) {
	        HashMap<String, String> map = new HashMap<>();

	        for (Nominee nominee : nominees) {
	            String nomineeName = nominee.getName();
	            map.put(nomineeName, "0");
	        }

	        return map;
	    }
	/**************************************************************************************************/
	
	   //Methode de l'authentification de l'admin
	public static boolean loginAdmin(String id, String password) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sql = "SELECT id, password FROM admin WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
              if (rs.next()) {
                String idDB = rs.getString("id");
                String passwordDB= rs.getString("password");
                if (idDB.equals(id) && passwordDB.equals(password)) {
                    return true; 
                }
            }
        }catch(Exception e) {
            System.out.println(e);
        }
        return false;
    }
	/**************************************************************************************************/
	//Methode de l'authentification du serveur
	public static boolean loginRespo(String id, String password) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sql = "SELECT server_id, server_password FROM server WHERE server_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
              if (rs.next()) {
                String idDB = rs.getString("server_id");
                String passwordDB= rs.getString("server_password");
                if (idDB.equals(id) && passwordDB.equals(password)) {
                    return true; 
                }
            }
        }catch(Exception e) {
            System.out.println(e);
        }
        return false;
    }
/**************************************************************************************************/
	
	//Methode qui permet d'enregistrer le choix du voteur 
	public static boolean updateChoice(int voterId, String nomineeID, String sessionKey) {
	    try {
	        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

	        String sql = "UPDATE voters SET nominee_ID = ?, session_Key = ? WHERE voter_id = ?";
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setString(1, nomineeID);
	        stmt.setString(2, sessionKey);
	        stmt.setInt(3, voterId);
	        int rowsUpdated = stmt.executeUpdate();
	        stmt.close();
	        conn.close();
	        return rowsUpdated > 0;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}
        /******************************************************************************/
	
	public static void main(String[] args) throws Exception {
		//recuperer ou generer les cles du serveur
		if(keysExist()){
			retrieveKeys();
		}else{
			keyPair = generateKey();
			serverPublicKey = (RSAPublicKey) keyPair.getPublic();
			serverPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
			saveServerKeys(Util.byteArrayToHexString(serverPrivateKey.getEncoded(), ""), Util.byteArrayToHexString(serverPublicKey.getEncoded(), ""));
		}
		//demarer la communication cleint-serveur
		startServer();

	}
	

	
	



}