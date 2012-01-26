<?php
 
header('Content-Type: text/plain');
 
require('opencalais.php');


$apikey = "hyh9hhy2eghaqcub5kdcs3ae";
$oc = new OpenCalais($apikey); 

$demotext = "Mexico expects the U.S. to accelerate the disbursement of aid to strengthen its fight against drug gangs and put back on track a $1.4 billion program that has been hamstrung by delays in recent years, Foreign Affairs Minister Patricia Espinosa said.

Espinosa, in an interview yesterday, said both U.S. President Barack Obama and Secretary of State Hillary Clinton promised to disburse $500 million this year in equipment and aid for police training as part of the bilateral Merida Initiative. Mexico expects to receive complete financing of the multi-year program by next year, she said.

We got off to a slow start in part because this is a completely new cooperation plan, Espinosa, 52, said at Bloomberg's offices in Mexico City. We now see that it's advancing more quickly.

U.S. anti-narcotics aid to Mexico suffered delays even as the death toll from President Felipe Calderon's crackdown on drug gangs surged to over 35,000 victims since he took office in 2006. Mexico received at least $480 million in U.S. aid under the program since it was signed in 2008 by Calderon and former President George W. Bush, with $380 million arriving between 2008 and 2010, according to data from the Foreign Ministry.

The shortfall in U.S. assistance has delayed the delivery of equipment including polygraph machines and Black Hawk helicopters needed to combat drug traffickers. It has also delayed the training of Mexican officials, according to the GAO report.

Until a year ago the U.S. had delivered only about 9 percent of the promised aid to Mexico and Central America because agencies involved lacked staff and funding, the U.S. Government Accountability Office said in a report in 2010. 

";
(!empty($_GET['input'])) ?  $text = $_GET['input'] : $text = $demotext; 
(!empty($_GET['prefix'])) ?  $prefix = $_GET['prefix'] : $prefix = "id:"; 

//?input-type=text&nif=true&prefix=&urirecipe=$urirecipe&input=

$text = $oc->getCalaisResult($text);
$prefix_nerd = "@prefix nerd: <http://nerd.eurecom.fr/ontology#> .";
$triples = array();

$ns = array(
       'rdf' => 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
       'rdfs' => 'http://www.w3.org/2000/01/rdf-schema#',
       'owl' => 'http://www.w3.org/2002/07/owl#',
       'sso' => 'http://nlp2rdf.lod2.eu/schema/sso/',
       'str' => 'http://nlp2rdf.lod2.eu/schema/string/',
       'topic' => 'http://nlp2rdf.lod2.eu/schema/topic/',
       'error' => 'http://nlp2rdf.lod2.eu/schema/error/',
       'olia' => 'http://purl.org/olia/olia.owl#',
       'olia-top' => 'http://purl.org/olia/olia-top.owl#',
       'olia_system' => 'http://purl.org/olia/system.owl#',
       'penn' => 'http://purl.org/olia/penn.owl#',
       'penn-syntax' => 'http://purl.org/olia/penn-syntax.owl#',
       'stanford' => 'http://purl.org/olia/stanford.owl#',
       'nerd'	=> 'http://nerd.eurecom.fr/ontology#',
       'brown' => 'http://purl.org/olia/brown.owl#');

$conf = array('ns' => $ns);


$parser = ARC2::getTurtleParser();
$parser->parse( $prefix, $text );
$triples = array_merge( $triples, $parser->getTriples());

$id=false; $ids = array(); $keys = array(); $os=-1; $exact = ''; $type = ''; $dict = array();

$temptriples = array();
$temptriple = array('type'=>'triple', 's'=>'', 'p'=>'http://s.opencalais.com/1/pred/nerd', 'o'=>'', 's_type'=>'uri', 'p_type'=>'uri', 'o_type'=>'literal', 'o_datatype'=>'', 'o_lang'=>'' );

$list = getNerdList();

foreach ($triples as $i=>$t) {
	if ($id===false || $id!==$t['s']) {
		if ($id!==false && $os>=0 && $exact!=='') {
			
			$id = $prefix.rawurlencode(substr('offset_'.$os.'_'.($os+strlen($exact)).'_'.$exact, 0, 1000));
			
			foreach ($keys as $k) {
				$triples[ $k ]['s'] = $id;
			}
		}
		
		if ($id!==false && $dict[ $type ]!==false) {
			$ids[$id] = $dict[ $type ];
		}

		$id = $t['s'];
		$keys = array();
		$os = -1;
		$exact = '';

		$type = explode('/', $t['o']);
		$type = trim( array_pop( $type ) );
		
		if (!isset($dict[$type])) {
			$dict[ $type ] = getNerdTerm( $type, $list );
		}
	}
	
	if (substr($t['p'], -11)== 'pred/offset') {
		$os = $t['o'];
	}
	else if (substr($t['p'], -10)== 'pred/exact') {
		$exact = $t['o'];
	}

	$keys[] = $i;
}

$ser = ARC2::getTurtleSerializer($conf);
$output = $prefix_nerd."\n".$ser->getSerializedTriples($triples);

foreach($ids as $id=>$term) {
	$find = "|\n<".$id."> rdf:type ([^:]+):([^ ]+) ;|";
	
	//KOMMA VARIANTE
	$replace = "<".$id."> rdf:type $1:$2, nerd:".$term." ;";

	$indent = preg_replace('|.|',' ',$id).'   ';
	
	//SEMIKOLON VARIANTE 1
	//$replace = "<".$id."> rdf:type $1:$2 ;\n".$indent."rdf:type nerd:".$term." ;";

	//SEMIKOLON VARIANTE 2
	//$replace = "<".$id."> rdf:type $1:$2 ;\n".$indent."nerd:class \"".$term."\" ;";
	
	$output = preg_replace($find, "\n".$replace, $output,1);
}

$dateiname = "output_calais.n3"; 
$handler = fOpen($dateiname , "w+");
fWrite($handler , $output);
fClose($handler); 
print $output;

function getNerdList() {
	$url = 'http://nerd.eurecom.fr/ui/ontology/nerd-last.n3';
	$url = 'list.txt';
	
	$list = @file_get_contents( $url );
	
	if ($list===false) die('unable to get nerd-list from '.$url);

	return $list;
}

function getNerdTerm( $t, $list ) {
	$default = false;

	$pos = strpos($list, 'opencalais:'.$t);
	if ($pos===false) return $default;
	$list = substr($list, 0, $pos );
	
	$pos = strrpos($list, 'nerd:');
	
	if ($pos===false) return $default;
	
	$term = substr($list, $pos+5);
	$term = explode(' ', $term);

	return trim($term[0]);
}




