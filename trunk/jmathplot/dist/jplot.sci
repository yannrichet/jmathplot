function _cmd = jplot2d(varargin)
_arg = " -2D -l SOUTH ";

i=1;
while (i<=length(varargin))
//disp(i);
	i_arg=0;
	q=%f;
	while ((length(varargin)>= i_arg+i+1) & (size(varargin(i+i_arg+1))==1))
	i_arg = i_arg+1;
	_arg = _arg + " " +varargin(i+i_arg);
	end
	
	_tmp(i) = ".xyz."+string(grand(1,1,"uin",0,10000))+".tmp";
	
	if ((size(varargin(i),2)==1))
	fprintfMat(_tmp(i),[(1:length(varargin(i)))' varargin(i)]);
	else
	fprintfMat(_tmp(i),varargin(i));
	end
	
	_arg = _arg + " " + _tmp(i);
	
	i = i+1+i_arg;
end

_cmd = "java -cp jmathplot.jar org.math.plot.PlotPanel"+_arg;
//disp(_cmd);
_res = unix_g(_cmd);
endfunction

function _cmd = jplot3d(varargin)
_arg = " -3D -l SOUTH ";

i=1;
while (i<=length(varargin))
	i_arg=0;
	while ((length(varargin)>= i_arg+i+1) & (size(varargin(i+i_arg+1))==1))
	i_arg = i_arg+1;
	_arg = _arg + " " +varargin(i+i_arg);
	end
	
	_tmp(i) = ".xyz."+string(grand(1,1,"uin",0,10000))+".tmp";

	fprintfMat(_tmp(i),varargin(i));

	_arg = _arg + " " + _tmp(i);
	
	i = i+1+i_arg;
end

_cmd = "java -cp jmathplot.jar org.math.plot.PlotPanel"+_arg;
//disp(_cmd);
_res = unix_g(_cmd);
endfunction