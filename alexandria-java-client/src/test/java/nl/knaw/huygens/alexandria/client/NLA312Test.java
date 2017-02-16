package nl.knaw.huygens.alexandria.client;

import nl.knaw.huygens.alexandria.test.AlexandriaTest;

public class NLA312Test extends AlexandriaTest {
  // TODO move to markup module
  // static OptimisticAlexandriaClient client;
  //
  // @BeforeClass
  // public static void startClient() {
  // client = new OptimisticAlexandriaClient("http://localhost:2015/");
  // client.setAuthKey("admin");
  // }
  //
  // @AfterClass
  // public static void stopClient() {
  // client.close();
  // }
  //
  //// @Test
  // public void testBugNLA332bw() {
  // String xml = singleQuotesToDouble("<TEI>\n"//
  // + "<teiHeader>\n"//
  // + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
  // + "<meta type='id' value='0001'/>\n"//
  // + "</teiHeader>\n"//
  // + "<text xml:id='text-1' lang='la'>\n"//
  // + "<body>\n"//
  // + "<div xml:id='div-1' type='letter'>\n"//
  // + "<p xml:id='p-1'>... ... Salmurij ...</p>\n"//
  // + "<p xml:id='p-2'><figure><graphic url='beec002jour04ill02.gif'/></figure></p>\n"//
  // + "</div>\n"//
  // + "</body>\n"//
  // + "</text>\n"//
  // + "</TEI>");
  // // ----
  // // UUID resourceUUID = createResourceWithText(xml);
  // // ----
  // UUID resourceUUID = UUID.randomUUID();
  // client.setResource(resourceUUID, resourceUUID.toString());
  // setResourceText(resourceUUID, xml);
  // // ----
  // client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("CKCC project team"));
  // client.setAnnotator(resourceUUID, "nerf", new Annotator().setCode("nerf").setDescription("Something"));
  // // ----
  // String textAsDot = client.getTextAsDot(resourceUUID);
  // Log.info("dot={}",textAsDot);
  // // ----
  // System.out.println(client.getTextAsString(resourceUUID));
  // // ----
  //
  // UUID annotationUUID1 = UUID.randomUUID();
  // TextRangeAnnotation.Position position1 = new TextRangeAnnotation.Position()//
  // .setXmlId("p-2");
  // Map<String, String> attributes1 = ImmutableMap.of("value", "closer");
  // TextRangeAnnotation closerAnnotation = new TextRangeAnnotation()//
  // .setId(annotationUUID1)//
  // .setName("p_type")//
  // .setAnnotator("ckcc")//
  // .setPosition(position1)//
  // .setAttributes(attributes1);
  // TextRangeAnnotationInfo info1 = client.setResourceTextRangeAnnotation(resourceUUID, closerAnnotation);
  // // ----
  // // assertThat(info1.getAnnotates()).isEqualTo("");
  // // ---
  // textAsDot = client.getTextAsDot(resourceUUID);
  // Log.info("dot={}",textAsDot);
  //
  // System.out.printf("annotated: [%s]%n", info1.getAnnotates());
  // if (!info1.getAnnotates().equals("")) {
  // System.out.println("ERROR");
  // }
  // // ----
  //
  // String textAfterFirstAnnotation = client.getTextAsString(resourceUUID);
  // String expectation1 = singleQuotesToDouble("<TEI>\n"//
  // + "<teiHeader>\n"//
  // + "<meta type='uuid' value='cd89eced-78d9-4a6a-9fa1-3857011e8ede'/>\n"//
  // + "<meta type='id' value='0001'/>\n"//
  // + "</teiHeader>\n"//
  // + "<text xml:id='text-1' lang='la'>\n"//
  // + "<body>\n"//
  // + "<div xml:id='div-1' type='letter'>\n"//
  // + "<p xml:id='p-1'>... ... Salmurij ...</p>\n"//
  // + "<p xml:id='p-2'><p_type value='closer' resp='#ckcc'><figure><graphic url='beec002jour04ill02.gif'/></figure></p_type></p>\n"//
  // + "</div>\n"//
  // + "</body>\n"//
  // + "</text>\n"//
  // + "</TEI>");
  // // ----
  // assertThat(textAfterFirstAnnotation).isEqualTo(expectation1);
  // // ----
  // System.out.println(textAfterFirstAnnotation);
  // if (!textAfterFirstAnnotation.equals(expectation1)) {
  // System.out.println("ERROR");
  // }
  // }
  //
  // /// end tests
  // // @Test
  // public void testNLA312() {
  // AboutEntity about = client.getAbout();
  // String xml = singleQuotesToDouble("<TEI>\n" //
  // + "<teiHeader>\n" //
  // + "<meta type='alt_id' value='arc766608'/>\n" //
  // + "<meta type='title' value='Bayle16750415_85'/>\n" //
  // + "<meta type='uuid' value='33375ef7-ca12-4624-950f-6a8e4e80aa96'/>\n" //
  // + "<meta type='id' value='0085'/>\n" //
  // + "<meta type='date' value='1675-04' precision='circa'/>\n" //
  // + "<meta type='sender' value='bayle.pierre.1647-1706'/>\n" //
  // + "<meta type='recipient' value='basnage-beauval.jacques.1653-1723'/>\n" //
  // + "<meta type='senderloc' value='se:paris.fra'/>\n" //
  // + "<meta type='recipientloc' value='se:sedan.fra'/>\n" //
  // + "<meta type='language' value='fr'/>\n" //
  // + "</teiHeader>\n" //
  // + "<text>\n" //
  // + "<body>\n" //
  // + "<div xml:id='div-1' type='letter' lang='fr'>\n" //
  // + "<p xml:id='p-1' rend='align-right'>A <placeName key='se:paris.fra'>Paris</placeName> avril 1675</p>\n" //
  // + "<p xml:id='p-2'>Mr Thiers a fait un livre <title>De stola</title><note type='explicative'>Jean-Baptiste Thiers (1636-1703), prêtre d'une exceptionnelle érudition, <title>De stola in
  // archidiaconorum visitationibus gestanda a paroecis disceptatio. In quâ multa ad Archidiaconorum munus, jurisdictionem ac Visitationem attinentia curiosè pertractantur</title> (Parisiis 1674,
  // 12°). Bayle admirera Thiers tout le long de sa carrière; voir, par exemple, <title>CPD</title>, cxxxi.</note> et a traitté la question s'il est permis aux curez de la porter pendant les visites
  // d'un archidiacre. Il est pour l'affirmative, et à l'occasion de cet ornement sacerdotal il rapporte mille choses savantes et curieuses des habits des pretres, tant parmi les juifs que parmi les
  // payens. J'ay eu si peu de loisir ces jours passez que je n'ai peu aller au Palais chez Mons<sup>r</sup> Billaine pour savoir la grandeur et le prix de l'<title>Origines rei
  // monasticae</title><note type='explicative'>Sur cet ouvrage, voir Lettre 83, n.18.</note>. J'ay veu tous les titres des chapitres sur une feuille volante, qui marquent un grand ordre et une grande
  // exactitude, et qui promettent bien des choses savantes. J'ai veu autrefois quelque chose de Mr Hauteserre c'est un homme de grande lecture et quand meme il y auroit du fatras dans son fait il ne
  // laisseroit pas d'enseigner de particularitez considerables. Je conjecture que c'est un in-4°. Mr Patin<note type='explicative'>Charles Patin (1633-1693), fils de Guy, finit ses jours comme
  // professeur à Padoue. Mêlé à une affaire de commerce de livres prohibés, il dut quitter précipitamment la France en 1668 pour échapper à une incarcération. Il circula dans divers pays d'Europe, et
  // passa donc probablement à Genève (dont Nyon, non loin de Coppet, est proche). Il raconte ses voyages dans ses <title>Quatre relations historiques</title> (Basle 1673, 12°); l'ouvrage reparut très
  // vite, sous un titre un peu modifié: <title>Relations historiques et curieuses de voyages en Allemagne, Angleterre, Hollande, Bohême, Suisse, &amp;c.</title> (Lyon 1674, 12°). Médecin de
  // formation, Charles Patin fut surtout un numismate d'une certaine notoriété; sur lui, voir <title>DHC</title>, «Patin (Guy)», rem. K, et Fr. Waquet, <title key='Waquet1979'>Charles Patin
  // (1633-1693)</title>. <title>Recherches sur la République des Lettres</title>, thèse dactylographiée de l'Ecole Pratique des Hautes Etudes, IVe section, avril 1979.</note> que nous rencontrames,
  // s'il vous souvient à Nyon, a fait imprimer une relation de son voyage en plusieurs cours d'<placeName key='co:deu'>Alemagne</placeName> où il loue terriblement les princes de cette nation. Je ne
  // sai du quel d'entre eux il dit qu'on remarque plutot en lui le heros que l'homme<note type='explicative'>C'est du roi Charles II que Charles Patin écrit, tout fier d'avoir rencontré le roi
  // d'Angleterre: «dans ce moment glorieux, j'aperceûs le héros avant le monarque» (<title>Quatre relations</title>, p.216). Bayle commet une confusion en croyant cette flagornerie inspirée par un
  // prince allemand.</note>. Il fait imprimer à <placeName key='se:basel.che'>Basle</placeName> un Suetone avec des medailles<note type='explicative'>Suétone, <title>Opera quae exstant. Carolus
  // Patinus ... notis et numismatibus illustravit suisque sumptibus edidit</title> (Basileae 1675, 4°).</note>. Graevius qui étoit bon ami de Mons<sup>r</sup> Le Fevre a donné au public son
  // Suetone<note type='explicative'>Johann Georg Graevius (1632-1703), professeur d'histoire à Utrecht, philologue de renommée européenne: <title>C. Suetonius Tranquillus ex recensione Joannis
  // Georgii Graevii, cum eiusdem animadversionibus, ut et commentario integro Laevini Torrentii et Isaaci Casauboni; his accedunt notae Theodori Marcilii et Francisci Guyeti, nec non index Matthiae
  // Bernecceri</title> (Trajecti ad Rhenum 1672, 4°).</note>.</p>\n" //
  // + "<p xml:id='p-3'>Je ne sai si vous avez veu la traduction qu'on a faite d'un livre du chevalier Temple Anglois touchant l'etat et le gouvernement present de la <placeName
  // key='co:nld'>Hollande</placeName><note type='explicative'>Sir William Temple (1628-1699), diplomate britannique, artisan de la Triple Alliance (Grande-Bretagne, Suède, Provinces-Unies) en 1668,
  // <title>L'Estat présent des Provinces Unies des Pays-Bas</title> (Paris 1674, 12°, 2 vol.), traduit de l'anglais par A. Le Vasseur. Cette édition parisienne omet un chapitre, consacré à la
  // religion, qui figure dans la traduction du même livre, éditée en Hollande sous le titre <title>Remarques sur l'estat des Provinces Unies des Païs-Bas, faites en l'an 1672 par M. le chevalier
  // Temple</title> (La Haye 1674, 8°) et, bien entendu, dans l'original anglais, <title>Observations upon the United Provinces of the Netherlands</title> (London 1673, 8°), ch. 5, p.189-208. Temple
  // admire la tolérance religieuse et civile des Provinces-Unies, et il signale que «no man can here complain of pressure in his conscience, of being forced to any publique profession of his private
  // faith» (p.205). On mesure ici l'autocensure provenant soit de l'éditeur, soit du traducteur de l'édition parisienne qui, bien avant la révocation de l'Edit de Nantes, engageait à ne rien publier
  // de flatteur ou même simplement de neutre concernant le protestantisme ou la tolérance civile.</note>. Il fut composé un an auparavant la guerre. J'ay leu il y a quinze jours un petit livre
  // imprimé il y a deux ans qui s'intitule <title>La Fatalité de St Clou</title><note type='explicative'>Bernard Guyard (1601-1674), dominicain: <title>La Fatalité de Saint Clou, pres Paris</title>
  // (s.l. 1672, 8°); l'auteur s'y efforçait de laver son ordre du régicide commis le 1er août 1589 sur la personne d'Henri III par Jacques Clément (1566?-1589), un dominicain, sous l'influence de
  // l'idéologie politique de la Ligue. Bayle fera de nouveau allusion à cet ouvrage anonyme dans <title>DHC</title>, «Henri III», rem. R.</note>: il tache de justifier que celui qui tua Henry 3me
  // n'étoit pas Jaques Clement. Il rapporte les principales circonstances de ce meurtre, et les accompagne de reflexions, tachant à tout le moins d'insinuer, qu'il y a lieu d'entrer en doutte si ceux
  // entre les mains de qui le <rs type='person' key='S0153008' resp='#ed'>moine</rs> tombat, ne le tuerent point la nuit, et ensuitte, revestirent de ses habits quelque couppe jarret apposté pour
  // tuer ce prince; ou bien si ceux qui introduiserent Jaques Clement dans le cabinet de Henry 3 ne tuerent point le roy tandis que ce monarque lisoit attentivement les lettres que le moine lui avoit
  // baillées, puis se mirent à crier que le moine avoit fait le coup et le tuerent sur le champ, de peur qu'il ne fit apparoir de son innocence.</p>\n" //
  // + "</div></body></text></TEI>");
  // UUID resourceUUID = createResourceWithText(xml);
  // client.setAnnotator(resourceUUID, "ckcc", new Annotator().setCode("ckcc").setDescription("Co Koccu"));
  //
  // UUID annotationUUID1 = UUID.randomUUID();
  // TextRangeAnnotation.Position position1 = new TextRangeAnnotation.Position()//
  // .setXmlId("p-1");
  // Map<String, String> attributes = ImmutableMap.of("value", "opener");
  // TextRangeAnnotation textRangeAnnotation = new TextRangeAnnotation()//
  // .setId(annotationUUID1)//
  // .setName("p_type")//
  // .setAnnotator("ckcc")//
  // .setPosition(position1)//
  // .setAttributes(attributes);
  // TextRangeAnnotationInfo info1 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation);
  // assertThat(info1.getAnnotates()).isEqualTo("A Paris avril 1675");
  // Log.info(info1.toString());
  //
  // UUID annotationUUID2 = UUID.randomUUID();
  // TextRangeAnnotation.Position position2 = new TextRangeAnnotation.Position()//
  // .setXmlId("p-2")//
  // .setOffset(4)//
  // .setLength(6);
  // TextRangeAnnotation persNameAnnotation = new TextRangeAnnotation()//
  // .setId(annotationUUID2)//
  // .setName("persName")//
  // .setAnnotator("ckcc")//
  // .setPosition(position2);
  // TextRangeAnnotationInfo info2 = client.setResourceTextRangeAnnotation(resourceUUID, persNameAnnotation);
  // assertThat(info2.getAnnotates()).isEqualTo("Thiers");
  // Log.info(info2.toString());
  //
  // UUID annotationUUID3 = UUID.randomUUID();
  // TextRangeAnnotation.Position position3 = new TextRangeAnnotation.Position()//
  // .setTargetAnnotationId(annotationUUID2);
  // Map<String, String> attributes3 = ImmutableMap.of("key", "value");
  // TextRangeAnnotation textRangeAnnotation2 = new TextRangeAnnotation()//
  // .setId(annotationUUID3)//
  // .setName("persName_key")//
  // .setAnnotator("ckcc")//
  // .setPosition(position3)//
  // .setAttributes(attributes3);
  // TextRangeAnnotationInfo info3 = client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation2);
  // Log.info(info3.toString());
  //
  // Map<String, String> attributes3a = ImmutableMap.of("key", "value2");
  // TextRangeAnnotation textRangeAnnotation2a = new TextRangeAnnotation()//
  // .setId(annotationUUID3)//
  // .setName("persName_key")//
  // .setAnnotator("ckcc")//
  // .setPosition(position3)//
  // .setAttributes(attributes3a);
  // client.setResourceTextRangeAnnotation(resourceUUID, textRangeAnnotation2a);
  //
  // String xml2 = client.getTextAsString(resourceUUID);
  // Log.info(xml2);
  //
  // // String expected = singleQuotesToDouble("<p xml:id='p-1'><persName_id id='W. Wortel (1934-)' resp='#ckcc'><persName resp='#ckcc'>Willie Wortel</persName></persName_id> vindt uit.</p>");
  // // assertThat(xml2).isEqualTo(expected);
  // }
  //
  // private UUID createResourceWithText(String xml) {
  // String resourceRef = "test";
  // UUID resourceUUID = createResource(resourceRef);
  // TextImportStatus textGraphImportStatus = setResourceText(resourceUUID, xml);
  // URI expectedURI = URI.create("http://localhost:2015/resources/" + resourceUUID + "/text/xml");
  // assertThat(textGraphImportStatus.getTextURI()).isEqualTo(expectedURI);
  // return resourceUUID;
  // }
  //
  // protected UUID createResource(String resourceRef) {
  // ResourcePrototype resource = new ResourcePrototype().setRef(resourceRef);
  // UUID resourceUuid = UUID.randomUUID();
  // client.setResource(resourceUuid, resource);
  // return resourceUuid;
  // }
  //
  // protected TextImportStatus setResourceText(UUID resourceUuid, String xml) {
  // TextImportStatus textImportStatus = client.setResourceTextSynchronously(resourceUuid, xml);
  // return textImportStatus;
  // }
  //
}
