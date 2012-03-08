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
(!empty($_GET['prefix'])) ?  $prefix = $_GET['prefix'] : $prefix = "https://github.com/alexlust/NIFOpenCalais#";
(!empty($_GET['urirecipe'])) ?  $urirecipe = $_GET['urirecipe'] : $urirecipe = 'offset';
(!empty($_GET['format'])) ?  $format = $_GET['format'] : $format = 'turtle';
(!empty($_GET['context-length'])) ?  $context_length = $_GET['context-length'] : $context_length = 10;


if (!(($urirecipe == 'offset') || ($urirecipe == 'context-hash'))) echo "ungültige Parameter: ".$urirecipe;
 
//$urirecipe = 'context-hash';
$text = $oc->getCalaisResult($text);
$prefix_nerd = "@prefix nerd: <http://nerd.eurecom.fr/ontology#> .";
$triples = array();

$ns = array(
       'rdf' => 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
	   'nerd' => 'http://nerd.eurecom.fr/ontology#',
       'rdfs' => 'http://www.w3.org/2000/01/rdf-schema#',
	   'foaf' => 'http://xmlns.com/foaf/0.1/',
	   'dc' => 'http://purl.org/dc/elements/1.1/');

$conf = array('ns' => $ns);


$parser = ARC2::getTurtleParser();
$parser->parse( $prefix, $text );
$triples = array_merge( $triples, $parser->getTriples());

$id=false; $ids = array(); $keys = array(); $os=-1; $exact = ''; $type = ''; $dict = array();

$list = getNerdList();


/*
for ($i = 0, $i_max = count($triples); $i < $i_max; $i++) 

          print_r($triples[$i]);
*/


foreach ($triples as $i=>$t) {
	if ($id===false || $id!==$t['s']) {
		if ($id!==false && $os>=0 && $exact!=='') {
			if ($urirecipe == 'context-hash') {$id = $prefix.'hash_'.$context_length.'_'.strlen($exact).'_'.md5(substr($detl, -$context_length).$exact.substr($detr, 0, $context_length)).'_'.rawurlencode(substr($exact, 0, 20));}
			else 
			$id = $prefix.rawurlencode(substr('offset_'.$os.'_'.($os+strlen($exact)).'_'.$exact, 0, 20));
			
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
    if (substr($t['p'], -14)== 'pred/detection') {
		$detection = $t['o'];
		$det1 = explode(']', $detection);
		$det2 = explode('[', $det1[0]);
		$detl = $det2[1];
		
		$det1 = explode('[', $detection);
		$det2 = explode(']', $det1[2]);
		$detr = $det2[0];
		
		
		
	}
	$keys[] = $i;
}

$ser = ARC2::getTurtleSerializer($conf);
$output = $ser->getSerializedTriples($triples);
$parser = ARC2::getRDFParser();
$parser->parse($prefix, $output);
$triples = $parser->getTriples();
for ($i = 0, $i_max = count($triples); $i < $i_max; $i++) {
  if ($triples[$i]['p'] == "http://s.opencalais.com/1/pred/id") $triples[$i]['o_type'] = "uri";
 // print_r($triples[$i]);
}
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
	
	$output = preg_replace($find, "\n".$replace, $output, 1);
}



$alltriples = array();
$new_triples = array();
//$temp_triple = array();
$parser = ARC2::getTurtleParser();
        $parser->parse($prefix, $output);
        $triples = $parser->getTriples();
		$triples1 = $parser->getTriples();
        $alltriples = array_merge($alltriples, $triples);
for ($i = 0, $i_max = count($alltriples); $i < $i_max; $i++) {
  if (stristr($triples[$i]['o'] , "http://nerd.eurecom.fr/ontology")) {
																			$nerd = $triples[$i]['o'];
																			$link = $triples[$i]['s'];
																			for ($j = 0, $j_max = count($alltriples); $j < $j_max; $j++)
																			{
																			if (stristr($triples1[$j]['s'] , $prefix ) && stristr($triples1[$j]['o'] , $link ))
																				{
																				$temp_triple = array("type"=>"triple", "s"=>$triples1[$j]['s'],  "p"=>$nerd, "o"=>$triples1[$j]['o'],  "s_type" => "uri", "p_type" => "uri", "o_type" => "uri", "o_datatype" => "", "o_lang"=>"");
																				array_push($new_triples, $temp_triple);
																				//print_r($temp_triple);
																				}
																			}
																			
																			}
  //print_r($alltriples[$i]);
  
}
$alltriples = array_merge($alltriples, $new_triples);

$ser = ARC2::getTurtleSerializer($conf);
$output = $ser->getSerializedTriples($alltriples);



switch ($format) {
    case 'rdfxml':
        $ser = ARC2::getRDFXMLSerializer($conf);
		$alltriples = array_merge($alltriples, $triples);
		$output = $ser->getSerializedTriples($alltriples);
        break;
    case 'ntriples':
        $parser = ARC2::getTurtleParser();
		$parser->parse($prefix, $output);
		$alltriples = array_merge($alltriples, $triples);
		$output = $ser->getNTriplesSerializer($alltriples);
		
        break;
	case 'turtle':
        $parser = ARC2::getTurtleParser();
        $parser->parse($prefix, $output);
        $triples = $parser->getTriples();
		$alltriples = array_merge($alltriples, $triples);
        break;
	case 'json':
        $parser = ARC2::getJSONParser();
		$parser->parse($prefix, $output);
        $triples = $parser->getTriples();
		$alltriples = array_merge($alltriples, $triples);
        break;
	default : 
		$parser = ARC2::getTurtleParser();
        $parser->parse($prefix, $output);
        $triples = $parser->getTriples();
		$alltriples = array_merge($alltriples, $triples);		
}

		
/*for ($i = 0, $i_max = count($triples); $i < $i_max; $i++) 

          print_r($triples[$i]);*/


		  
		  
		  
$dateiname = "output_calais.n3"; 
$handler = fOpen($dateiname , "w+");
fWrite($handler , $output);
fClose($handler); 

print $output;

function getNerdList() {
	$url = 'http://nerd.eurecom.fr/ui/ontology/nerd-last.n3';
	$list = @file_get_contents( $url );
	
	//$list = file_get_contents( 'list.txt' );
	/*
	$prefix = "https://github.com/alexlust/NIFOpenCalais";
	
	$alltriples = array();
	$parser = ARC2::getTurtleParser();
    $parser->parse($prefix, $list);
    $triples = $parser->getTriples();
    $alltriples = array_merge($alltriples, $triples);
	for ($i = 0, $i_max = count($alltriples); $i < $i_max; $i++) {
	//if ($triples[$i]['p'] == "http://s.opencalais.com/1/pred/id") $triples[$i]['o_type'] = "uri";
	print_r($alltriples[$i]);
	}
	*/
	
	
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




