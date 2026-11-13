import re, glob, os
D = glob.glob(os.path.expanduser('~/mnt/*/_alp/Agents/CoperXXI'))[0]
def rd(p): return open(os.path.join(D,p),encoding='utf-8',newline='').read().replace('\r\n','\n')
def wr(p,s): open(os.path.join(D,p),'w',encoding='utf-8',newline='').write(s.replace('\r\n','\n').replace('\n','\r\n'))

NID=[1794600000000]
def nid():
    NID[0]+=1; return str(NID[0])

# ---------------- EmbeddedObjects ----------------
eo = rd('EmbeddedObjects.xml')
parts = re.split(r'(?=\t<EmbeddedObject>\n)', eo)
head, objs = parts[0], parts[1:]
def name(o): return re.search(r'<Name><!\[CDATA\[(.*?)\]\]></Name>',o).group(1)
idx = {name(o):i for i,o in enumerate(objs)}
def get(n): return objs[idx[n]]
def put(n,o): objs[idx[n]] = o

def param_xml(p, code, unit=None):
    if code is None:
        return '<Parameter>\n\t\t\t\t<Name><![CDATA[%s]]></Name>\n\t\t\t</Parameter>' % p
    if unit:
        return ('<Parameter>\n\t\t\t\t<Name><![CDATA[%s]]></Name>\n\t\t\t\t<Value Class="CodeUnitValue">\n\t\t\t\t\t<Code><![CDATA[%s]]></Code>\n'
                '\t\t\t\t\t<Unit Class="TimeUnits">%s</Unit>\n\t\t\t\t</Value>\n\t\t\t</Parameter>') % (p, code, unit)
    return ('<Parameter>\n\t\t\t\t<Name><![CDATA[%s]]></Name>\n\t\t\t\t<Value Class="CodeValue">\n\t\t\t\t\t<Code><![CDATA[%s]]></Code>\n'
            '\t\t\t\t</Value>\n\t\t\t</Parameter>') % (p, code)

def set_param(o, p, code, unit=None):
    pat = re.compile(r'<Parameter>\s*<Name><!\[CDATA\[%s\]\]></Name>.*?</Parameter>' % re.escape(p), re.S)
    assert len(pat.findall(o))==1, ('param', p)
    return pat.sub(lambda m: param_xml(p, code, unit), o)

def get_code(o, p):
    m = re.search(r'<Parameter>\s*<Name><!\[CDATA\[%s\]\]></Name>\s*<Value Class="CodeValue">\s*<Code><!\[CDATA\[(.*?)\]\]></Code>' % re.escape(p), o, re.S)
    return m.group(1)

def repl_code(n, p, old, new):
    o = get(n); c = get_code(o, p); assert old in c, (n,p,old); put(n, set_param(o, p, c.replace(old,new)))

# 1) estoques: Hold -> Wait
wait_tpl = get('espera_segunda_parte')
wait_params = re.search(r'<Parameters>.*?</Parameters>', wait_tpl, re.S).group(0)
for l in ('b','a','moega'):
    n = 'estoque_'+l
    o = get(n)
    assert '<ClassName>Hold</ClassName>' in o
    o = o.replace('<ClassName>Hold</ClassName>','<ClassName>Wait</ClassName>').replace('<ItemName>1412336242940</ItemName>','<ItemName>1412336243169</ItemName>')
    o = re.sub(r'<Parameters>.*?</Parameters>', lambda m: wait_params, o, flags=re.S)
    o = set_param(o,'onEnter','log_analise("%s", agent, "esperando");' % n)
    o = set_param(o,'onExit','log_analise("%s", agent, "liberado");' % n)
    put(n,o)

repl_code('acoplar_b','onEnter1','estoque_b.unblock();','liberar_estoque("b");')
repl_code('acoplar_a','onEnter1','estoque_a.unblock();','liberar_estoque("a");')
repl_code('acoplar_moega','onEnter1','estoque_moega.unblock();','liberar_estoque("moega");')
repl_code('para_estoque_moega','onExit','estoque_moega.block();\n','')

# 2) alívio engata tudo e corta só no fim
o = get('precisa_mais_vgs')
o = set_param(o,'condition','// engata todos os lotes (b -> a -> moega) antes de cortar a sobra\nainda_tem_lote_vazios()')
put('precisa_mais_vgs', o)
repl_code('rmv_excesso','nCars','Math.max(0, get_vgs_trazidos_alivio((Trem) train) - quantidade_alivio)',
          'ainda_tem_lote_vazios() ? 0 : Math.max(0, get_vgs_trazidos_alivio((Trem) train) - quantidade_alivio)')
repl_code('para_alivio_seguir2','onExit','// ainda falta vagão: engata o próximo lote','// ainda tem lote de vazios: engata o próximo')
repl_code('para_fim_alivio','onExit','// já leva o que o GestorDeAlivios pediu','// engatou tudo; o rmv_excesso já cortou a sobra')
repl_code('iniciar_alivio','onEnter','qt_vgs_inicio_alivio = ((Trem) agent).getNumeroDeVagoes();',
          'qt_vgs_inicio_alivio = ((Trem) agent).getNumeroDeVagoes();\ntrem_alivio = (Trem) agent;')
repl_code('retornar_gestorDeAlivios','onExit','liberar_espaco_vazios();\niniciar_proximo_lote();',
          '// com sobra, a descarga só volta quando a sobra for guardada (guardar_sobra)\nif (trem_sobra == null) {\n\tliberar_espaco_vazios();\n\tiniciar_proximo_lote();\n}')

# 3) blocos novos da sobra
def clone(tpl, newname, X, Y, params):
    o = get(tpl).replace('</EmbeddedObjects>\n','')
    o = re.sub(r'<Name><!\[CDATA\[%s\]\]></Name>' % re.escape(tpl), '<Name><![CDATA[%s]]></Name>' % newname, o, count=1)
    o = re.sub(r'(\t\t<X>)-?\d+(</X>\n\t\t<Y>)-?\d+(</Y>)', r'\g<1>%d\g<2>%d\g<3>' % (X,Y), o, count=1)
    o = re.sub(r'<Id>\d+</Id>', lambda m: '<Id>%s</Id>' % nid(), o)
    for p in re.findall(r'<Parameter>\s*<Name><!\[CDATA\[(.*?)\]\]></Name>', o):
        if p.startswith('on') and p not in params:
            o = set_param(o, p, None)
    for p,v in params.items():
        if isinstance(v, tuple): o = set_param(o, p, v[0], v[1])
        else: o = set_param(o, p, v)
    return o

L = lambda blk, msg: 'log_analise("%s", agent, "%s");' % (blk, msg)
LT = lambda blk, msg, v='train': 'log_analise("%s", %s, "%s");' % (blk, v, msg)
novos = [
 clone('entrar_rmv_excesso','entrar_sobra',4600,560, {'onEnter': L('entrar_sobra','sobra do alívio')}),
 clone('precisa_mais_vgs','linha_livre_sobra',4750,560, {
     'condition':'// a loco do alívio já saiu da psnSaidaCopexXXI_b?\nlinha_livre_para_sobra()',
     'onExitTrue': L('linha_livre_sobra','true'), 'onExitFalse': L('linha_livre_sobra','false')}),
 clone('descarga_grupo','espera_linha_sobra',4750,660, {
     'delayTime': ('30','SECOND'), 'onEnter': L('espera_linha_sobra','inicio'), 'onExit': L('espera_linha_sobra','fim')}),
 clone('ir_lote_b','ir_sobra',4900,560, {
     'onEnter': LT('ir_sobra','inicio'), 'onExit': LT('ir_sobra','chegou'), 'onExitHit': LT('ir_sobra','bateu (outHit)')}),
 clone('precisa_mais_vgs','sobra_cabe',5050,560, {
     'condition':'// o que resta da sobra cabe inteiro na próxima linha (b -> a -> moega)?\n((Trem) agent).getNumeroDeVagoes() <= get_capacidade_linha(sobra_linha_alvo())',
     'onExitTrue': L('sobra_cabe','true'), 'onExitFalse': L('sobra_cabe','false')}),
 clone('rmv_excesso','cortar_sobra',5150,660, {
     'nCars':'get_capacidade_linha(sobra_linha_alvo())',
     'onEnter': LT('cortar_sobra','vai cortar'), 'onExit': LT('cortar_sobra','parte que segue'),
     'onExitDecoupled':'// a parte cortada (frente, lado do lote_b) fica com a linha alvo antes de o resto voltar ao sobra_cabe\nregistrar_sobra((Trem) train2);\n' + LT('cortar_sobra','parte cortada','train2')}),
 clone('para_guardar_excesso','para_guardar_sobra',5350,560, {'onExit':'guardar_sobra((Trem) agent, true);'}),
 clone('para_guardar_excesso','para_guardar_sobra_parte',5350,660, {'onExit':'guardar_sobra((Trem) agent, false);'}),
]
last = objs[-1]
objs[-1] = last.replace('\t</EmbeddedObject>\n</EmbeddedObjects>','\t</EmbeddedObject>\n' + ''.join(novos).rstrip('\n') + '\n</EmbeddedObjects>')
assert objs[-1] != last
eo_new = head + ''.join(objs)
POS = {}
for o in re.split(r'(?=\t<EmbeddedObject>\n)', eo_new)[1:]:
    n = name(o); X,Y = map(int, re.search(r'\t\t<X>(-?\d+)</X>\n\t\t<Y>(-?\d+)</Y>', o).groups()); POS[n]=(X,Y)
wr('EmbeddedObjects.xml', eo_new)

# ---------------- Connectors ----------------
cx = rd('Connectors.xml')
cs = re.split(r'(?=\t<Connector>\n)', cx)
for i,c in enumerate(cs):
    if re.search(r'<ItemName>estoque_(b|a|moega)</ItemName>', c):
        cs[i] = c.replace('<ClassName>Hold</ClassName>','<ClassName>Wait</ClassName>')
cx = ''.join(cs)
cnum = [max(int(i) for i in re.findall(r'connector(\d+)', cx))]
PM, RL = 'com.anylogic.libraries.processmodeling', 'com.anylogic.libraries.rail'
CLS = {'entrar_sobra':(PM,'Enter'),'linha_livre_sobra':(PM,'SelectOutput'),'espera_linha_sobra':(PM,'Delay'),
       'ir_sobra':(RL,'TrainMoveTo'),'sobra_cabe':(PM,'SelectOutput'),'cortar_sobra':(RL,'TrainDecouple'),
       'para_guardar_sobra':(PM,'Exit'),'para_guardar_sobra_parte':(PM,'Exit')}
def conn(src, sp, dst, dp):
    cnum[0]+=1
    sx,sy = POS[src]; tx,ty = POS[dst]
    x0,y0 = sx+20, sy; x1,y1 = tx-20, ty
    if tx <= sx:
        pts = [(0,0),(10,0),(10,60),(x1-x0-10,60),(x1-x0-10,y1-y0),(x1-x0,y1-y0)]
    elif y1 != y0:
        pts = [(0,0),(0,y1-y0),(x1-x0,y1-y0)]
    else:
        pts = [(0,0),(x1-x0,0)]
    sP,sC = CLS[src]; tP,tC = CLS[dst]
    p = ''.join('\t\t\t<Point>\n\t\t\t\t<X>%d</X>\n\t\t\t\t<Y>%d</Y>\n\t\t\t</Point>\n' % q for q in pts)
    return ('\t<Connector>\n\t\t<Id>%s</Id>\n\t\t<Name><![CDATA[connector%d]]></Name>\n\t\t<X>%d</X>\n\t\t<Y>%d</Y>\n'
            '\t\t<Label>\n\t\t\t<X>10</X>\n\t\t\t<Y>0</Y>\n\t\t</Label>\n\t\t<PublicFlag>false</PublicFlag>\n\t\t<PresentationFlag>true</PresentationFlag>\n\t\t<ShowLabel>false</ShowLabel>\n'
            '\t\t<SourceEmbeddedObjectReference>\n\t\t\t<PackageName>margemdireitamodel</PackageName>\n\t\t\t<ClassName>CoperXXI</ClassName>\n\t\t\t<ItemName>%s</ItemName>\n\t\t</SourceEmbeddedObjectReference>\n'
            '\t\t<SourceConnectableItemReference>\n\t\t\t<PackageName>%s</PackageName>\n\t\t\t<ClassName>%s</ClassName>\n\t\t\t<ItemName>%s</ItemName>\n\t\t</SourceConnectableItemReference>\n'
            '\t\t<TargetEmbeddedObjectReference>\n\t\t\t<PackageName>margemdireitamodel</PackageName>\n\t\t\t<ClassName>CoperXXI</ClassName>\n\t\t\t<ItemName>%s</ItemName>\n\t\t</TargetEmbeddedObjectReference>\n'
            '\t\t<TargetConnectableItemReference>\n\t\t\t<PackageName>%s</PackageName>\n\t\t\t<ClassName>%s</ClassName>\n\t\t\t<ItemName>%s</ItemName>\n\t\t</TargetConnectableItemReference>\n'
            '\t\t<Points>\n%s\t\t</Points>\n\t</Connector>\n') % (nid(), cnum[0], x0, y0, src, sP, sC, sp, dst, tP, tC, dp, p)
links = [('entrar_sobra','out','linha_livre_sobra','in'),
         ('linha_livre_sobra','outT','ir_sobra','in'),
         ('linha_livre_sobra','outF','espera_linha_sobra','in'),
         ('espera_linha_sobra','out','linha_livre_sobra','in'),
         ('ir_sobra','out','sobra_cabe','in'),
         ('ir_sobra','outHit','sobra_cabe','in'),
         ('sobra_cabe','outT','para_guardar_sobra','in'),
         ('sobra_cabe','outF','cortar_sobra','in'),
         ('cortar_sobra','out','sobra_cabe','in'),
         ('cortar_sobra','outDecoupled','para_guardar_sobra_parte','in')]
add = ''.join(conn(*l) for l in links)
assert '\t</Connector>\n</Connectors>' in cx
cx = cx.replace('\t</Connector>\n</Connectors>', '\t</Connector>\n' + add + '</Connectors>')
wr('Connectors.xml', cx)

# ---------------- Variables ----------------
vx = rd('Variables.xml')
i = vx.find('[lote_vazios_b]'); s = vx.rfind('\t<Variable', 0, i); e = vx.find('</Variable>', i) + len('</Variable>\n')
tpl = vx[s:e]
nv = ''
for nm, y in (('trem_alivio',1840),('trem_sobra',1870)):
    v = tpl.replace('[lote_vazios_b]', '[%s]' % nm)
    v = re.sub(r'<Id>\d+</Id>', lambda m: '<Id>%s</Id>' % nid(), v, count=1)
    v = re.sub(r'(\t\t<X>)-?\d+(</X>\n\t\t<Y>)-?\d+', r'\g<1>1860\g<2>%d' % y, v, count=1)
    nv += v
assert '</Variables>' in vx
vx = vx.replace('</Variables>', nv + '</Variables>')
wr('Variables.xml', vx)

# ---------------- Functions ----------------
fx = rd('Code/Functions.xml'); fj = rd('Code/Functions.java')
def addf(nm, ret, params, body, y):
    fid = nid()
    mod = 'VOID' if ret == 'void' else 'RETURNS_VALUE'
    rt = 'double' if ret == 'void' else ret
    px = ''.join('\t\t<Parameter>\n\t\t\t<Name><![CDATA[%s]]></Name>\n\t\t\t<Type><![CDATA[%s]]></Type>\n\t\t</Parameter>\n' % (pn,pt) for pt,pn in params)
    xml = ('\t<Function AccessType="default" StaticFunction="false">\n\t\t<ReturnModificator>%s</ReturnModificator>\n\t\t<ReturnType>%s</ReturnType>\n'
           '\t\t<Id>%s</Id>\n\t\t<Name><![CDATA[%s]]></Name>\n\t\t<X>4400</X>\n\t\t<Y>%d</Y>\n\t\t<Label>\n\t\t\t<X>10</X>\n\t\t\t<Y>0</Y>\n\t\t</Label>\n'
           '\t\t<PublicFlag>false</PublicFlag>\n\t\t<PresentationFlag>true</PresentationFlag>\n\t\t<ShowLabel>true</ShowLabel>\n%s'
           '\t\t<Body xmlns:al="http://anylogic.com"/>\n\t</Function>\n') % (mod, rt, fid, nm, y, px)
    java = '%s %s(%s)\n{/*ALCODESTART::%s*/\n%s\n/*ALCODEEND*/}\n\n' % (rt, nm, ','.join('%s %s' % p for p in params), fid, body.strip('\n'))
    return xml, java

F = [
('ainda_tem_lote_vazios','boolean',[],'''
// ainda há lote de vazios parado em b, a ou moega? O alívio engata todos antes de cortar a sobra
return lote_vazios_b != null || lote_vazios_a != null || lote_vazios_moega != null;
'''),
('linha_livre_para_sobra','boolean',[],'''
// a sobra só anda depois que o trem do alívio saiu da psnSaidaCopexXXI_b (as duas pontas fora da linha)
if (fazendoAlivio) return false;
if (trem_alivio == null) return true;
try {
	RailwayTrack linha = main().psnSaidaCopexXXI_b;
	return trem_alivio.getTrack(true) != linha && trem_alivio.getTrack(false) != linha;
} catch (Exception e) {
	// o trem do alívio já não existe (acoplado nas DVs ou removido)
	return true;
}
'''),
('get_capacidade_linha','int',[('String','linha')],'''
// capacidade de vazios de cada posição da linha
if ("b".equals(linha)) return CAPACIDADE_LOTE_B;
if ("a".equals(linha)) return CAPACIDADE_LOTE_A;
return get_capacidade_moega();
'''),
('sobra_linha_alvo','String',[],'''
// a sobra enche b, depois a, depois o lote da moega
if (lote_vazios_b == null) return "b";
if (lote_vazios_a == null) return "a";
return "moega";
'''),
('registrar_sobra','void',[('Trem','trem')],'''
// marca o pedaço da sobra com a linha alvo e grava o lote de vazios dela
String linha = sobra_linha_alvo();
trem.tipoDeLote = "coperXII_vazios_" + linha;
if ("b".equals(linha))      lote_vazios_b = trem;
else if ("a".equals(linha)) lote_vazios_a = trem;
else                        lote_vazios_moega = trem;
'''),
('guardar_sobra','void',[('Trem','trem'),('boolean','ultima')],'''
// guarda um pedaço da sobra no estoque da linha dele; o último pedaço retoma a descarga
if (trem != lote_vazios_b && trem != lote_vazios_a && trem != lote_vazios_moega) registrar_sobra(trem);
if (trem == lote_vazios_b)      entrar_estoque_b.take(trem);
else if (trem == lote_vazios_a) entrar_estoque_a.take(trem);
else                            entrar_estoque_moega.take(trem);
Log.info("CoperXXI", "sobra_guardada", String.format("trem=%d %d vgs em %s | b=%d a=%d moega=%d",
	trem.getId(), trem.getNumeroDeVagoes(), trem.tipoDeLote,
	CAPACIDADE_LOTE_B - get_espaco_linha("b"), CAPACIDADE_LOTE_A - get_espaco_linha("a"), get_capacidade_moega() - get_espaco_linha("moega")));
if (ultima) {
	trem_sobra = null;
	verificar_alivio();
	liberar_espaco_vazios();
	iniciar_proximo_lote();
}
'''),
('liberar_estoque','void',[('String','linha')],'''
// solta o lote de vazios parado no Wait da linha para o TrainCouple
if ("b".equals(linha))      { if (estoque_b.size() > 0)     estoque_b.free(estoque_b.get(0)); }
else if ("a".equals(linha)) { if (estoque_a.size() > 0)     estoque_a.free(estoque_a.get(0)); }
else                        { if (estoque_moega.size() > 0) estoque_moega.free(estoque_moega.get(0)); }
'''),
]
addx = ''; addj = ''
for k,(nm,ret,ps,body) in enumerate(F):
    a,b = addf(nm,ret,ps,body,1690+30*k); addx += a; addj += b
assert '</Functions>' in fx
fx = fx.replace('</Functions>', addx + '</Functions>')
old_gv = '''// ALT-065: guarda o lote na linha (Hold bloqueado) e solta o resto do lote que espera na moega
if ("coperXII_vazios_b".equals(trem.tipoDeLote)) {
	estoque_b.block();
	entrar_estoque_b.take(trem);
} else {
	estoque_a.block();
	entrar_estoque_a.take(trem);
}'''
new_gv = '''// ALT-065: guarda o lote na linha (Wait do estoque) e solta o resto do lote que espera na moega
if ("coperXII_vazios_b".equals(trem.tipoDeLote)) {
	entrar_estoque_b.take(trem);
} else {
	entrar_estoque_a.take(trem);
}'''
assert old_gv in fj; fj = fj.replace(old_gv, new_gv)
m = re.search(r'(double guardar_excesso_alivio\(Trem lote\)\n\{/\*ALCODESTART::\d+\*/\n)(.*?)(\n/\*ALCODEEND\*/\})', fj, re.S)
assert m
fj = fj[:m.start(2)] + '''// sobra do alívio (rabo do trem, lado da moega): espera a loco sair da linha,
// anda até o lote_b e é repartida em b -> a -> moega (sobra_cabe / cortar_sobra / guardar_sobra)
trem_sobra = lote;
lote.tipoDeLote = "coperXII_sobra";
Log.info("CoperXXI", "alivio_sobra", String.format("trem=%d sobra de %d vgs (pedido=%d)",
	lote.getId(), lote.getNumeroDeVagoes(), quantidade_alivio));
entrar_sobra.take(lote);''' + fj[m.end(2):]
fj = fj.rstrip('\n') + '\n\n' + addj.rstrip('\n') + '\n'
wr('Code/Functions.xml', fx); wr('Code/Functions.java', fj)
print('ok', NID[0], cnum[0])
