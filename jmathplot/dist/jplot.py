import os
import scipy

def jplot2d(*args):
	arg=' -2D -l SOUTH '
	i=0
	while (i<len(args)):
		i_arg=0
		q=False
		arg_txt=True
		while ((len(args)>(i_arg+i+1)) & arg_txt):
			if (type(args[i+i_arg+1]) is str):
				i_arg+=1
				arg=arg+' '+args[i+i_arg]
			else:
				arg_txt=False
				
		name='.'+str(scipy.stats.random_integers(10000))+'.plot.tmp'
		
		if ((len(args[i])==scipy.size(args[i]))):
			a=scipy.dstack((scipy.arange(1,scipy.size(args[i])+1),args[i]))[0]
			scipy.io.write_array(name,a)
		else:
			scipy.io.write_array(name,args[i])
			
		i+=i_arg+1
		arg=arg+' '+name
	
	os.system('java -cp jmathplot.jar org.math.plot.PlotPanel '+arg)

def jplot3d(*args):
	arg=' -3D -l SOUTH '
	i=0
	while (i<len(args)):
		i_arg=0
		q=False
		arg_txt=True
		while ((len(args)>(i_arg+i+1)) & arg_txt):
			if (type(args[i+i_arg+1]) is str):
				i_arg+=1
				arg=arg+' '+args[i+i_arg]
			else:
				arg_txt=False
				
		name='.'+str(scipy.stats.random_integers(10000))+'.plot.tmp'
		
		scipy.io.write_array(name,args[i])
			
		i+=i_arg+1
		arg=arg+' '+name
	
	os.system('java -cp jmathplot.jar org.math.plot.PlotPanel '+arg)
