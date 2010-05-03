#!/usr/bin/env perl
#
# filter to transform mysql ddl into oracle ddl
# author: Dennis Box, dbox@fnal.gov
# date: Dec 2001
# usage MysqlToOracleFilter.pl inputfile.mysql > inputfile.ora_sql
#
use strict;

#my $data_tablespace='MINOSDEV_DATA';
#my $idx_tablespace='MINOSDEV_IDX';
my $data_tablespace='MINOS_DEV_DATA';
my $idx_tablespace='MINOS_DEV_IDX';
my $line;
my $big_line;
my $table=0;
my $pk_name=0;
my $fk_name=0;
my $old_table=0;
my $col_flag=0;
my $next_col=0;
my $trigger_yada="
DROP SEQUENCE _SEQ_NAME_;

CREATE SEQUENCE _SEQ_NAME_
 INCREMENT BY 1
 START WITH 1
 NOMAXVALUE
 MINVALUE 1
 NOCYCLE
 NOCACHE
/ 
DROP  PUBLIC SYNONYM  _SEQ_NAME_;
CREATE PUBLIC SYNONYM  _SEQ_NAME_ FOR  _SEQ_NAME_ ;

CREATE OR REPLACE TRIGGER _TRG_NAME_
BEFORE INSERT
ON _TAB_NAME_
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW
BEGIN
DECLARE
NEXT         NUMBER;
BEGIN
SELECT _SEQ_NAME_.NEXTVAL INTO NEXT FROM DUAL;
:new.ROW_COUNTER:=NEXT;
END;
END _TRG_NAME_;
/

DROP PUBLIC SYNONYM  _TRG_NAME_ ;
CREATE PUBLIC SYNONYM  _TRG_NAME_ FOR  _TRG_NAME_ ;
";



if($ENV{'DATA_TABLESPACE'}){
  $data_tablespace=$ENV{'DATA_TABLESPACE'};
}
if($ENV{'INDEX_TABLESPACE'}){
  $idx_tablespace=$ENV{'INDEX_TABLESPACE'};
}


print "SET ECHO ON;\n";
print "ALTER SESSION SET NLS_DATE_FORMAT='YYYY-MM-DD hh24:mi:ss';\n";
while (<>){
  $line = $_;
  $line = "\U$_" unless "\U$line" =~/^INSERT INTO/;
  $line =~ s/\`//g;
  $line =~ s/.* KEY .*//g unless $line =~/ PRIMARY /;
  $line =~ s/ IF EXISTS / /g;
  $line =~ s/ IF NOT EXISTS / /g;
  $line =~ s/ DATETIME/ DATE/g;
  $line =~ s/ UNSIGNED//g;
  $line =~ s/ TINYINT/ number/g;
  $line =~ s/ SMALLINT/ number/g;
  $line =~ s/ INT\(/ number\(/g;
  $line =~ s/   VIEW/    View_dir/g;
  $line =~ s/VALIDITY/VLD/g;
  $line =~ s/VALID/VLD/g;
  #$line =~ s/ DOUBLE/ BINARY_DOUBLE/g;
  $line =~ s/ TEXT/ VARCHAR(2000)/g;
  $line =~ s/\#/-- /;
  $line =~ s/ TINYTEXT/ VARCHAR(200)/g;
  $line =~ s/MODE /TASK /g;
  #$line =~ s/ CHAR\(/ VARCHAR\(/g;
  $line =~ s/AUTO_INCREMENT//g;
  $line =~ s/UNSIGNED//g;
  $line =~ s/^\>\>/-- /g;
  $line =~ s/^\<\</-- /g;
  $line =~ s/TYPE=MYISAM//g;
  $line =~ s/, INDEX \(SEQNO\)//g;
  $line =~ s/'(\d\d\d\d-\d\d-\d\d) (\d\d:\d\d:\d\d)'/to_date\('$1 $2','YYYY-MM-DD hh24:mi:ss'\) /g;
  $line =~ s/0000-00-00/0001-01-01/g;
  $line =~ s/KEY SEQNO \(SEQNO\)/CONSTRAINT $fk_name FOREIGN KEY\(SEQNO\) REFERENCES $table.VLD\(SEQNO\)/g;
  $line =~ s/PRIMARY KEY  \(SEQNO\)/CONSTRAINT $pk_name PRIMARY KEY\(SEQNO\) USING INDEX TABLESPACE $idx_tablespace/g;
  $line =~ s/PRIMARY KEY  \(SEQNO,ROW_COUNTER\)/CONSTRAINT $pk_name PRIMARY KEY\(SEQNO,ROW_COUNTER\) USING INDEX TABLESPACE $idx_tablespace/g;
  $line =~ s/,$// if $line =~/CONSTRAINT/;

  $line =~ s/\.VLD/VLD/g;
  $line =~ s/COMMENT/RUN_COMMENT/g;
  $line =~ s/ENGINE=.*$/;/g;
  #$line =~ s/FLOAT\(.*\)/BINARY_FLOAT/g;
  $line =~ s/FLOAT\(.*\)/FLOAT/g;
  $line =~ s/BIGINT\(.*\)/INTEGER/g;
  #$line =~ s/FLOAT /BINARY_FLOAT /g;
  $line =~ s/NOT NULL DEFAULT(.*,$)/ DEFAULT $1 NOT NULL, /;
  $line =~ s/NOT NULL DEFAULT(.*$)/ DEFAULT $1 NOT NULL /;
  $line =~ s/\, NOT NULL/NOT NULL/g;
  $line =~ s/\\N/NULL/g;
  $line =~ s/^\) ;/\)TABLESPACE $data_tablespace;/g;

  if($line =~/SEQNO number/){
    $col_flag=1;
    $next_col='';
  }



  if("\U$line" =~ /CREATE/ and  "\U$line" =~ /TABLE/){
    if(!("\U$line" =~/VLD/)){
     # $line = $line."\tROW_COUNTER INTEGER DEFAULT NULL,\n";
    }
    $old_table=$table;
    my @parts = split(/\s+/,$line);
    $table = $parts[2];
    $table =~ s/\(.*//;
    $fk_name = 'FK_'.$table;
    $pk_name = 'PK_'.$table;
    $fk_name =~s/[aeiouAEIOU]+//g if length($fk_name)>25;
    $pk_name =~s/[aeiouAEIOU]+//g if length($pk_name)>25;
    if($old_table ne 0){
      print "\Ugrant  select on $old_table to minos_reader;\n";
      print "\Ugrant  select,insert,update on $old_table to minos_writer;\n";
      print "\Ucreate public synonym $old_table for $old_table;\n" ;
    
      if(!($old_table =~/VLD/)){
	my $idx = $old_table.'_idx';
	my $trigger_incant=&make_trigger($old_table);
	$idx=~s/[aeiouAEIOU]+//g if length($idx)>25;
	#print "ALTER TABLE $old_table ADD CONSTRAINT $pk_name PRIMARY KEY(SEQNO,ROW_COUNTER) using index tablespace  $idx_tablespace;  \n";
	print "\Ucreate index $idx  on ";
	print " $old_table(seqno) tablespace $idx_tablespace;  \n\n\n";
	#print "$trigger_incant";
      }else{
	#print "ALTER TABLE $old_table ADD CONSTRAINT $pk_name PRIMARY KEY(SEQNO) using index tablespace  $idx_tablespace;  \n";
      }
    }


    if($table ne 0){
      my $tab_ref = $table;
      $tab_ref =~ s/VLD//;
      if($tab_ref ne $table){
	print "\Udrop table $tab_ref;\n" ;
	print "\Udrop public synonym $tab_ref;\n\n";
	print "\Udrop table $table;\n" ;
	print "\Udrop public synonym $table;\n\n";
      }
    }
  }
  next if  "\U$line" =~ /^\s+$/;
  next if  "\U$line" =~ /^DROP TABLE/;
  next if  "\U$line" =~ /^DESCRIBE/;
  next if "\U$line" =~ /^USE/;
  next if "\U$line" =~ /^#/;
  #print $line;
  $big_line=$big_line.$line;
}
print $big_line;
$big_line='';
print "create public synonym $table for $table;\n" if ($table);
print "grant  select on $table to minos_reader;\n" if ($table);
print "grant  select,insert,update on $table to minos_writer;\n" if ($table);
my $idx = $table.'_idx';

$idx=~s/[aeiouAEIOU]+//g if length($idx)>25;
if($table && !($table =~/VLD/)){
  my $trigger_incant=&make_trigger($table);
  #print "ALTER TABLE $table ADD CONSTRAINT $pk_name \n"; 
  #print "PRIMARY KEY(SEQNO,ROW_COUNTER) using index tablespace  $idx_tablespace;\n";
  print "create index $idx  on $table(seqno) tablespace $idx_tablespace; \n\n";
  #print "$trigger_incant";

}

 

print "EXIT;\n";


sub make_trigger()
  {
    my ($table) = @_;
    my $stable=$table;
    $stable=~s/[aeiouAEIOU]+//g if length($stable)>25;

    my $trg=$stable.'_TRG';
    my $seq=$stable.'_SEQ';
    my $incant=$trigger_yada;
    $incant =~s/_SEQ_NAME_/$seq/g;
    $incant =~s/_TRG_NAME_/$trg/g;
    $incant =~s/_TAB_NAME_/$table/g;
    $incant;
  }
